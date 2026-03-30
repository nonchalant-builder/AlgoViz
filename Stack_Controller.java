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

public class Stack_Controller {

    // ── FXML fields ───────────────────────────────────────────────
    @FXML private Button   home_btn;
    @FXML private Button   pushBtn;
    @FXML private Button   popBtn;
    @FXML private Button   peekBtn;

    @FXML private TextField valueField;
    @FXML private TextField maxSizeField;

    // Info bar labels
    @FXML private Label sizeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label topLabel;
    @FXML private Label isEmptyLabel;
    @FXML private Label isFullLabel;


    @FXML private Pane  stackVisualPane;
    @FXML private ListView<String> operationLog;

    // ── Stack data ────────────────────────────────────────────────
    // index 0 = bottom of stack, last index = top of stack
    private ArrayList<Integer> stack = new ArrayList<>();
    private int maxSize = 10;
    private boolean isAnimating = false;

    // ── Colours ───────────────────────────────────────────────────
    private Color BLUE   = Color.web("#4FC3F7"); // normal element
    private Color GREEN  = Color.web("#66BB6A"); // just pushed
    private Color RED    = Color.web("#EF5350"); // being popped
    private Color YELLOW = Color.web("#FFD54F"); // peek / top
    private Color ORANGE = Color.web("#FF5722"); // overflow error
    private Color DARK   = Color.web("#263238"); // empty slot

    // ─────────────────────────────────────────────────────────────
    //  INITIALIZE
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        drawStack(-1, BLUE);
        updateInfoBar();
        addLog("Enter a value and press ⬆ Push, or use ⬇ Pop / 👁 Peek.");
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
            addLog("⚠  Please enter a max size.");
            return;
        }

        int newSize;
        try {
            newSize = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            addLog("⚠  Max size must be a number.");
            return;
        }

        if (newSize < 1 || newSize > 20) {
            addLog("⚠  Max size must be between 1 and 20.");
            return;
        }

        maxSize = newSize;
        // Trim stack from top if it exceeds new size
        while (stack.size() > maxSize) {
            stack.remove(stack.size() - 1);
        }

        drawStack(-1, BLUE);
        updateInfoBar();
        addLog("✔  Capacity set to " + maxSize + ".");
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onClearClicked() {
        if (isAnimating) return;
        stack.clear();
        drawStack(-1, BLUE);
        updateInfoBar();
        addLog("Cleared stack");
    }

    // ─────────────────────────────────────────────────────────────
    //  PUSH
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onPushClicked() {
        if (isAnimating) return;

        // Validate input
        String input = valueField.getText().trim();
        if (input.isEmpty()) {
            addLog("warn ⚠");
            addLog( "Enter a value to push.");
            return;
        }

        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            addLog("warn");
            addLog("⚠  Value must be a whole number.");
            return;
        }

        // Overflow check
        if (stack.size() >= maxSize) {
            addLog("error 🚫 "+"Stack Overflow! Stack is full (capacity = " + maxSize + ").");
            drawStack(-1, ORANGE);
            Timeline revert = new Timeline(
                    new KeyFrame(Duration.millis(600), e -> drawStack(-1, BLUE))
            );
            revert.play();
            return;
        }

        isAnimating = true;
        lockButtons();

        stack.add(value);
        int topIndex = stack.size() - 1;
        drawStack(topIndex, GREEN);
        addLog("⬆  Pushing " + value + " onto the stack...");

        Timeline finish = new Timeline(
                new KeyFrame(Duration.millis(700), e -> {
                    drawStack(-1, BLUE);
                    updateInfoBar();
                    addLog("Pushed: " + value);
                    valueField.clear();
                    unlockButtons();
                    isAnimating = false;
                })
        );
        finish.play();
    }


    @FXML
    private void onPopClicked() {
        if (isAnimating) return;

        if (stack.isEmpty()) {
           addLog("error"+" 🚫 Stack Underflow! Stack is empty.");
            return;
        }

        isAnimating = true;
        lockButtons();

        int topIndex = stack.size() - 1;
        int topValue = stack.get(topIndex);

        drawStack(topIndex, RED);
        addLog("⬇  Popping " + topValue + " from the stack...");

        Timeline remove = new Timeline(
                new KeyFrame(Duration.millis(700), e -> {
                    stack.remove(topIndex);
                    drawStack(-1, BLUE);
                    updateInfoBar();
                    addLog("Popped: " + topValue);
                    addLog("✅  Popped " + topValue + ". Size = " + stack.size() + ".");
                    unlockButtons();
                    isAnimating = false;
                })
        );
        remove.play();
    }

    @FXML
    private void onPeekClicked() {
        if (isAnimating) return;

        if (stack.isEmpty()) {
            addLog("Stack is empty — nothing to peek.");
            return;
        }

        isAnimating = true;
        lockButtons();

        int topIndex = stack.size() - 1;
        int topValue = stack.get(topIndex);

        drawStack(topIndex, YELLOW);
        addLog("👁  Peek: top element is " + topValue + " at index " + topIndex + ".");
        addLog("Peek: " + topValue);

        Timeline revert = new Timeline(
                new KeyFrame(Duration.millis(1200), e -> {
                    drawStack(-1, BLUE);
                    unlockButtons();
                    isAnimating = false;
                })
        );
        revert.play();
    }

    private void drawStack(int highlightIndex, Color highlightColor) {
        stackVisualPane.getChildren().clear();

        double paneW = stackVisualPane.getWidth()  > 0 ? stackVisualPane.getWidth()  : 1000.0;
        double paneH = stackVisualPane.getHeight() > 0 ? stackVisualPane.getHeight() : 500.0;

        double cellHeight = 52;
        double cellGap    = 6;
        double cellWidth  = Math.min((paneW - 80) / maxSize - cellGap, 100);
        double totalWidth = maxSize * (cellWidth + cellGap) - cellGap;
        double startX     = (paneW - totalWidth) / 2.0;
        double cellY      = paneH / 2.0 - cellHeight / 2.0 + 30;

        for (int i = 0; i < maxSize; i++) {
            double x = startX + i * (cellWidth + cellGap);
            boolean hasValue = (i < stack.size());

            // Determine cell colour
            Color fillColor;
            if (!hasValue) {
                fillColor = DARK;
            } else if (i == highlightIndex) {
                fillColor = highlightColor;
            } else {
                // When highlightIndex == -1 and highlightColor is ORANGE, flash all filled cells
                if (highlightIndex == -1 && highlightColor == ORANGE) {
                    fillColor = ORANGE;
                } else {
                    fillColor = BLUE;
                }
            }

            // Draw cell rectangle
            Rectangle cell = new Rectangle(x, cellY, cellWidth, cellHeight);
            cell.setFill(fillColor);
            cell.setArcWidth(8);
            cell.setArcHeight(8);
            cell.setStroke(Color.web("rgba(123,47,247,0.5)"));
            cell.setStrokeWidth(1.5);
            stackVisualPane.getChildren().add(cell);

            // Index label below cell
            Text indexText = new Text(String.valueOf(i));
            indexText.setFont(Font.font("Verdana", FontWeight.NORMAL, 11));
            indexText.setFill(Color.web("#7b2ff7"));
            double iw = indexText.getLayoutBounds().getWidth();
            indexText.setX(x + (cellWidth - iw) / 2.0);
            indexText.setY(cellY + cellHeight + 18);
            stackVisualPane.getChildren().add(indexText);

            // Value inside cell
            if (hasValue) {
                Text valueText = new Text(String.valueOf(stack.get(i)));
                valueText.setFont(Font.font("Verdana", FontWeight.BOLD,
                        Math.min(cellWidth * 0.35, 18)));
                valueText.setFill(Color.WHITE);
                double vw = valueText.getLayoutBounds().getWidth();
                double vh = valueText.getLayoutBounds().getHeight();
                valueText.setX(x + (cellWidth - vw) / 2.0);
                valueText.setY(cellY + cellHeight / 2.0 + vh / 4.0);
                stackVisualPane.getChildren().add(valueText);
            }
        }

        // TOP arrow above last filled cell
        if (!stack.isEmpty()) {
            int topIdx   = stack.size() - 1;
            double arrowX = startX + topIdx * (cellWidth + cellGap) + cellWidth / 2.0;

            Rectangle shaft = new Rectangle(arrowX - 2, cellY - 36, 4, 28);
            shaft.setFill(YELLOW);
            stackVisualPane.getChildren().add(shaft);

            Text arrow = new Text(arrowX - 8, cellY - 6, "▼");
            arrow.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            arrow.setFill(YELLOW);
            stackVisualPane.getChildren().add(arrow);

            Text topText = new Text("TOP");
            topText.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            topText.setFill(YELLOW);
            double tw = topText.getLayoutBounds().getWidth();
            topText.setX(arrowX - tw / 2.0);
            topText.setY(cellY - 42);
            stackVisualPane.getChildren().add(topText);
        }

        // BOTTOM label on far left
        Text bottomLabel = new Text("BOTTOM");
        bottomLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        bottomLabel.setFill(Color.web("rgba(123,47,247,0.6)"));
        bottomLabel.setX(startX);
        bottomLabel.setY(cellY + cellHeight + 36);
        stackVisualPane.getChildren().add(bottomLabel);

        // TOP label on far right
        double rightEdge = startX + maxSize * (cellWidth + cellGap);
        Text topSide = new Text("← TOP");
        topSide.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        topSide.setFill(Color.web("rgba(123,47,247,0.6)"));
        double tsw = topSide.getLayoutBounds().getWidth();
        topSide.setX(rightEdge - tsw - 4);
        topSide.setY(cellY + cellHeight + 36);
        stackVisualPane.getChildren().add(topSide);
    }

    private void updateInfoBar() {
        int size = stack.size();

        sizeLabel.setText(String.valueOf(size));
        capacityLabel.setText(String.valueOf(maxSize));
        topLabel.setText(size > 0 ? String.valueOf(stack.get(size - 1)) : "—");

        if (size == 0) {
            isEmptyLabel.setText("true");
            isEmptyLabel.setTextFill(Color.web("#66BB6A")); // green when empty
        } else {
            isEmptyLabel.setText("false");
            isEmptyLabel.setTextFill(Color.web("#4FC3F7"));
        }

        if (size >= maxSize) {
            isFullLabel.setText("true");
            isFullLabel.setTextFill(Color.web("#FF5722")); // orange when full
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


    private void lockButtons() {
        pushBtn.setDisable(true);
        popBtn.setDisable(true);
        peekBtn.setDisable(true);
    }

    private void unlockButtons() {
        pushBtn.setDisable(false);
        popBtn.setDisable(false);
        peekBtn.setDisable(false);
    }
}