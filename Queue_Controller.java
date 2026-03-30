package com.algoviz.algoviz;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;

public class Queue_Controller {

    // ── FXML fields ───────────────────────────────────────────────
    @FXML private Button home_btn;
    @FXML private Button enqueueBtn;
    @FXML private Button dequeueBtn;
    @FXML private Button peekBtn;

    @FXML private TextField valueField;
    @FXML private TextField maxSizeField;

    // Info bar labels
    @FXML private Label sizeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label frontLabel;
    @FXML private Label rearLabel;
    @FXML private Label isEmptyLabel;
    @FXML private Label isFullLabel;
    @FXML private Label statusDot;
    @FXML private Label statusLabel;

    @FXML private Pane queueVisualPane;
    @FXML private ListView<String> operationLog;

    // ── Queue data ────────────────────────────────────────────────
    // index 0 = FRONT (next to dequeue), last index = REAR (last enqueued)
    private ArrayList<Integer> queue = new ArrayList<>();
    private int maxSize = 10;
    private boolean isAnimating = false;

    // ── Colours ───────────────────────────────────────────────────
    private Color BLUE   = Color.web("#4FC3F7"); // normal element
    private Color GREEN  = Color.web("#66BB6A"); // just enqueued (rear)
    private Color RED    = Color.web("#EF5350"); // being dequeued (front)
    private Color YELLOW = Color.web("#FFD54F"); // peek front
    private Color ORANGE = Color.web("#FF5722"); // overflow / underflow
    private Color DARK   = Color.web("#263238"); // empty slot

    // ─────────────────────────────────────────────────────────────
    //  INITIALIZE
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        drawQueue(-1, BLUE);
        updateInfoBar();
        setStatus("idle", "Enter a value and press ➡ Enqueue, or use ⬅ Dequeue / 👁 Peek Front.");
    }

    // ─────────────────────────────────────────────────────────────
    //  HOME
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("sortingAlgorithm");
    }

    // ─────────────────────────────────────────────────────────────
    //  SET SIZE
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onSetSizeClicked() {
        if (isAnimating) return;

        String input = maxSizeField.getText().trim();
        if (input.isEmpty()) {
            setStatus("warn", "⚠  Please enter a max size.");
            return;
        }

        int newSize;
        try {
            newSize = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            setStatus("warn", "⚠  Max size must be a number.");
            return;
        }

        if (newSize < 1 || newSize > 20) {
            setStatus("warn", "⚠  Max size must be between 1 and 20.");
            return;
        }

        maxSize = newSize;
        // Trim from rear if queue exceeds new size
        while (queue.size() > maxSize) {
            queue.remove(queue.size() - 1);
        }

        drawQueue(-1, BLUE);
        updateInfoBar();
        setStatus("ok", "✔  Capacity set to " + maxSize + ".");
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onClearClicked() {
        if (isAnimating) return;
        queue.clear();
        drawQueue(-1, BLUE);
        updateInfoBar();
        addLog("Cleared queue");
        setStatus("idle", "🗑  Queue cleared.");
    }

    // ─────────────────────────────────────────────────────────────
    //  ENQUEUE — adds to REAR (end of list)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onEnqueueClicked() {
        if (isAnimating) return;

        String input = valueField.getText().trim();
        if (input.isEmpty()) {
            setStatus("warn", "⚠  Enter a value to enqueue.");
            return;
        }

        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            setStatus("warn", "⚠  Value must be a whole number.");
            return;
        }

        // Overflow check
        if (queue.size() >= maxSize) {
            setStatus("error", "🚫  Queue Overflow! Queue is full (capacity = " + maxSize + ").");
            drawQueue(-1, ORANGE);
            Timeline revert = new Timeline(
                    new KeyFrame(Duration.millis(600), e -> drawQueue(-1, BLUE))
            );
            revert.play();
            return;
        }

        // Animation — highlight new rear element green, then revert
        isAnimating = true;
        lockButtons();

        queue.add(value);
        int rearIndex = queue.size() - 1;
        drawQueue(rearIndex, GREEN);
        setStatus("ok", "➡  Enqueueing " + value + " at the rear...");

        Timeline finish = new Timeline(
                new KeyFrame(Duration.millis(700), e -> {
                    drawQueue(-1, BLUE);
                    updateInfoBar();
                    addLog("Enqueued: " + value);
                    setStatus("ok", "✅  Enqueued " + value + " at rear. Size = " + queue.size() + ".");
                    valueField.clear();
                    unlockButtons();
                    isAnimating = false;
                })
        );
        finish.play();
    }

    // ─────────────────────────────────────────────────────────────
    //  DEQUEUE — removes from FRONT (index 0)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onDequeueClicked() {
        if (isAnimating) return;

        if (queue.isEmpty()) {
            setStatus("error", "🚫  Queue Underflow! Queue is empty.");
            return;
        }

        isAnimating = true;
        lockButtons();

        int frontValue = queue.get(0);

        // Highlight front red, then remove it
        drawQueue(0, RED);
        setStatus("ok", "⬅  Dequeuing " + frontValue + " from the front...");

        Timeline remove = new Timeline(
                new KeyFrame(Duration.millis(700), e -> {
                    queue.remove(0);
                    drawQueue(-1, BLUE);
                    updateInfoBar();
                    addLog("Dequeued: " + frontValue);
                    setStatus("ok", "✅  Dequeued " + frontValue + " from front. Size = " + queue.size() + ".");
                    unlockButtons();
                    isAnimating = false;
                })
        );
        remove.play();
    }

    // ─────────────────────────────────────────────────────────────
    //  PEEK — shows FRONT without removing
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onPeekClicked() {
        if (isAnimating) return;

        if (queue.isEmpty()) {
            setStatus("error", "🚫  Queue is empty — nothing to peek.");
            return;
        }

        isAnimating = true;
        lockButtons();

        int frontValue = queue.get(0);

        drawQueue(0, YELLOW);
        setStatus("ok", "👁  Peek: front element is " + frontValue + " at index 0.");
        addLog("Peek front: " + frontValue);

        Timeline revert = new Timeline(
                new KeyFrame(Duration.millis(1200), e -> {
                    drawQueue(-1, BLUE);
                    unlockButtons();
                    isAnimating = false;
                })
        );
        revert.play();
    }

    // ─────────────────────────────────────────────────────────────
    //  DRAW THE QUEUE
    //
    //  Horizontal array: index 0 = FRONT (left), last filled = REAR (right)
    //  FRONT arrow (green) above index 0
    //  REAR  arrow (blue)  above last filled index
    // ─────────────────────────────────────────────────────────────
    private void drawQueue(int highlightIndex, Color highlightColor) {
        queueVisualPane.getChildren().clear();

        double paneW = queueVisualPane.getWidth()  > 0 ? queueVisualPane.getWidth()  : 1000.0;
        double paneH = queueVisualPane.getHeight() > 0 ? queueVisualPane.getHeight() : 500.0;

        double cellHeight = 52;
        double cellGap    = 6;
        double cellWidth  = Math.min((paneW - 80) / maxSize - cellGap, 100);
        double totalWidth = maxSize * (cellWidth + cellGap) - cellGap;
        double startX     = (paneW - totalWidth) / 2.0;
        double cellY      = paneH / 2.0 - cellHeight / 2.0 + 30;

        for (int i = 0; i < maxSize; i++) {
            double x = startX + i * (cellWidth + cellGap);
            boolean hasValue = (i < queue.size());

            Color fillColor;
            if (!hasValue) {
                fillColor = DARK;
            } else if (i == highlightIndex) {
                fillColor = highlightColor;
            } else {
                if (highlightIndex == -1 && highlightColor == ORANGE) {
                    fillColor = ORANGE;
                } else {
                    fillColor = BLUE;
                }
            }

            // Draw cell
            Rectangle cell = new Rectangle(x, cellY, cellWidth, cellHeight);
            cell.setFill(fillColor);
            cell.setArcWidth(8);
            cell.setArcHeight(8);
            cell.setStroke(Color.web("rgba(123,47,247,0.5)"));
            cell.setStrokeWidth(1.5);
            queueVisualPane.getChildren().add(cell);

            // Index label below
            Text indexText = new Text(String.valueOf(i));
            indexText.setFont(Font.font("Verdana", FontWeight.NORMAL, 11));
            indexText.setFill(Color.web("#7b2ff7"));
            double iw = indexText.getLayoutBounds().getWidth();
            indexText.setX(x + (cellWidth - iw) / 2.0);
            indexText.setY(cellY + cellHeight + 18);
            queueVisualPane.getChildren().add(indexText);

            // Value inside cell
            if (hasValue) {
                Text valueText = new Text(String.valueOf(queue.get(i)));
                valueText.setFont(Font.font("Verdana", FontWeight.BOLD,
                        Math.min(cellWidth * 0.35, 18)));
                valueText.setFill(Color.WHITE);
                double vw = valueText.getLayoutBounds().getWidth();
                double vh = valueText.getLayoutBounds().getHeight();
                valueText.setX(x + (cellWidth - vw) / 2.0);
                valueText.setY(cellY + cellHeight / 2.0 + vh / 4.0);
                queueVisualPane.getChildren().add(valueText);
            }
        }

        // FRONT arrow above index 0 (green)
        if (!queue.isEmpty()) {
            double frontX = startX + cellWidth / 2.0;

            Rectangle shaft = new Rectangle(frontX - 2, cellY - 36, 4, 28);
            shaft.setFill(GREEN);
            queueVisualPane.getChildren().add(shaft);

            Text arrow = new Text(frontX - 8, cellY - 6, "▼");
            arrow.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            arrow.setFill(GREEN);
            queueVisualPane.getChildren().add(arrow);

            Text label = new Text("FRONT");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            label.setFill(GREEN);
            double lw = label.getLayoutBounds().getWidth();
            label.setX(frontX - lw / 2.0);
            label.setY(cellY - 42);
            queueVisualPane.getChildren().add(label);
        }

        // REAR arrow above last filled index (blue)
        if (queue.size() > 1) {
            int rearIdx   = queue.size() - 1;
            double rearX  = startX + rearIdx * (cellWidth + cellGap) + cellWidth / 2.0;

            Rectangle shaft = new Rectangle(rearX - 2, cellY - 36, 4, 28);
            shaft.setFill(BLUE);
            queueVisualPane.getChildren().add(shaft);

            Text arrow = new Text(rearX - 8, cellY - 6, "▼");
            arrow.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            arrow.setFill(BLUE);
            queueVisualPane.getChildren().add(arrow);

            Text label = new Text("REAR");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            label.setFill(BLUE);
            double lw = label.getLayoutBounds().getWidth();
            label.setX(rearX - lw / 2.0);
            label.setY(cellY - 42);
            queueVisualPane.getChildren().add(label);
        }

        // "FRONT →" on far left
        Text leftLabel = new Text("FRONT →");
        leftLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        leftLabel.setFill(Color.web("rgba(102,187,106,0.6)"));
        leftLabel.setX(startX);
        leftLabel.setY(cellY + cellHeight + 36);
        queueVisualPane.getChildren().add(leftLabel);

        // "← REAR" on far right
        double rightEdge = startX + maxSize * (cellWidth + cellGap);
        Text rightLabel  = new Text("← REAR");
        rightLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        rightLabel.setFill(Color.web("rgba(79,195,247,0.6)"));
        double rlw = rightLabel.getLayoutBounds().getWidth();
        rightLabel.setX(rightEdge - rlw - 4);
        rightLabel.setY(cellY + cellHeight + 36);
        queueVisualPane.getChildren().add(rightLabel);
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE INFO BAR
    // ─────────────────────────────────────────────────────────────
    private void updateInfoBar() {
        int size = queue.size();

        sizeLabel.setText(String.valueOf(size));
        capacityLabel.setText(String.valueOf(maxSize));
        frontLabel.setText(size > 0 ? String.valueOf(queue.get(0))        : "—");
        rearLabel.setText( size > 0 ? String.valueOf(queue.get(size - 1)) : "—");

        if (size == 0) {
            isEmptyLabel.setText("true");
            isEmptyLabel.setTextFill(Color.web("#66BB6A"));
        } else {
            isEmptyLabel.setText("false");
            isEmptyLabel.setTextFill(Color.web("#4FC3F7"));
        }

        if (size >= maxSize) {
            isFullLabel.setText("true");
            isFullLabel.setTextFill(Color.web("#FF5722"));
        } else {
            isFullLabel.setText("false");
            isFullLabel.setTextFill(Color.web("#4FC3F7"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private void addLog(String message) {
        operationLog.getItems().add(0, message);
    }

    private void setStatus(String level, String message) {
        statusLabel.setText(message);
        switch (level) {
            case "ok"    -> statusDot.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 10px;");
            case "warn"  -> statusDot.setStyle("-fx-text-fill: #FFD54F; -fx-font-size: 10px;");
            case "error" -> statusDot.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 10px;");
            default      -> statusDot.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");
        }
    }

    private void lockButtons() {
        enqueueBtn.setDisable(true);
        dequeueBtn.setDisable(true);
        peekBtn.setDisable(true);
    }

    private void unlockButtons() {
        enqueueBtn.setDisable(false);
        dequeueBtn.setDisable(false);
        peekBtn.setDisable(false);
    }
}