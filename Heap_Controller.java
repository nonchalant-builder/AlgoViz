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

public class Heap_Controller {

    @FXML private Button insertBtn;
    @FXML private Button removeBtn;
    @FXML private Button peekBtn;
    @FXML private Button buildHeapBtn;
    @FXML private Button clearBtn;
    @FXML private Button home_btn;
    @FXML private Button toggleBtn;

    @FXML private TextField valueField;
    @FXML private TextField buildField;
    @FXML private TextField maxSizeField;

    @FXML private Label typeLabel;
    @FXML private Label sizeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label rootLabel;
    @FXML private Label isEmptyLabel;
    @FXML private Label isFullLabel;
    @FXML private Label statusDot;
    @FXML private Label statusLabel;

    @FXML private Pane heapVisualPane;
    @FXML private ListView<String> operationLog;


    private ArrayList<Integer> heap = new ArrayList<>();
    private int maxSize     = 15;
    private boolean isMinHeap   = true;
    private boolean isAnimating = false;

    private Color BLUE   = Color.web("#4FC3F7");
    private Color GREEN  = Color.web("#66BB6A");
    private Color RED    = Color.web("#EF5350");
    private Color YELLOW = Color.web("#FFD54F");
    private Color ORANGE = Color.web("#FF5722");
    private Color DARK   = Color.web("#263238");

    @FXML
    public void initialize() {
        drawHeap(-1, BLUE);
        updateInfoPanel();
        setStatus("idle", "Enter a value and press ➕ Insert, or use 🔨 Build Heap.");
        toggleBtn.setText("⇅  Min Heap");
        typeLabel.setText("Min Heap");
    }

    // ─────────────────────────────────────────────────────────────
    //  HOME
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("OpeningScene");
    }

    @FXML
    private void onInsertClicked() {
        if (isAnimating) return;

        String input = valueField.getText().trim();
        if (input.isEmpty()) { setStatus("warn", "⚠  Enter a value to insert."); return; }

        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            setStatus("warn", "⚠  Value must be a whole number.");
            return;
        }

        if (heap.size() >= maxSize) {
            setStatus("error", "🚫  Heap is full (capacity = " + maxSize + ").");
            drawHeap(-1, ORANGE);
            new Timeline(new KeyFrame(Duration.millis(600), e -> drawHeap(-1, BLUE))).play();
            return;
        }

        isAnimating = true;
        lockButtons();

        // Add to end, highlight green, then heapify up after 600ms
        heap.add(value);
        int newIndex = heap.size() - 1;
        drawHeap(newIndex, GREEN);
        setStatus("ok", "➕  Inserting " + value + " at index " + newIndex + "...");

        new Timeline(new KeyFrame(Duration.millis(600), e -> {
            heapifyUp(newIndex);
            drawHeap(-1, BLUE);
            updateInfoPanel();
            addLog("Inserted: " + value);
            setStatus("ok", "✅  Inserted " + value + ". Size = " + heap.size() + ".");
            valueField.clear();
            unlockButtons();
            isAnimating = false;
        })).play();
    }

    // ─────────────────────────────────────────────────────────────
    //  REMOVE ROOT
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onRemoveClicked() {
        if (isAnimating) return;

        if (heap.isEmpty()) { setStatus("error", "🚫  Heap is empty — nothing to remove."); return; }

        isAnimating = true;
        lockButtons();

        int rootValue = heap.get(0);

        // Highlight root red, then remove after 700ms
        drawHeap(0, RED);
        setStatus("ok", "➖  Removing root " + rootValue + "...");

        new Timeline(new KeyFrame(Duration.millis(700), e -> {
            int lastIndex = heap.size() - 1;
            if (lastIndex == 0) {
                heap.remove(0);
            } else {
                heap.set(0, heap.get(lastIndex));
                heap.remove(lastIndex);
                heapifyDown(0);
            }
            drawHeap(-1, BLUE);
            updateInfoPanel();
            addLog("Removed root: " + rootValue);
            setStatus("ok", "✅  Removed root " + rootValue + ". Size = " + heap.size() + ".");
            unlockButtons();
            isAnimating = false;
        })).play();
    }

    // ─────────────────────────────────────────────────────────────
    //  PEEK ROOT
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onGetSmallestClicked() {
        if (isAnimating) return;

        if (heap.isEmpty()) { setStatus("error", "🚫  Heap is empty — nothing to peek."); return; }

        isAnimating = true;
        lockButtons();

        int rootValue = heap.get(0);
        String label  = isMinHeap ? "minimum" : "maximum";

        drawHeap(0, YELLOW);
        setStatus("ok", "👁  Root (" + label + ") = " + rootValue + " at index 0.");
        addLog("Peek root: " + rootValue);

        new Timeline(new KeyFrame(Duration.millis(1200), e -> {
            drawHeap(-1, BLUE);
            unlockButtons();
            isAnimating = false;
        })).play();
    }

    // ─────────────────────────────────────────────────────────────
    //  BUILD HEAP
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onBuildHeapClicked() {
        if (isAnimating) return;

        String input = buildField.getText().trim();
        if (input.isEmpty()) {
            setStatus("warn", "⚠  Enter comma-separated values in the Build Heap field.");
            return;
        }

        // Parse all comma-separated numbers
        String[] parts = input.split(",");
        ArrayList<Integer> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            try {
                values.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException e) {
                setStatus("warn", "⚠  All values must be numbers. Found: \"" + trimmed + "\"");
                return;
            }
        }

        if (values.isEmpty()) { setStatus("warn", "⚠  No valid numbers found."); return; }
        if (values.size() > maxSize) {
            setStatus("warn", "⚠  Too many values (" + values.size() + "). Max = " + maxSize + ".");
            return;
        }

        // Load values then heapify bottom-up (last non-leaf down to root)
        heap.clear();
        heap.addAll(values);
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapifyDown(i);
        }

        drawHeap(-1, BLUE);
        updateInfoPanel();
        addLog("Built heap from: " + input);
        buildField.clear();
        setStatus("ok", "✅  Built " + (isMinHeap ? "Min" : "Max")
                + " Heap from " + heap.size() + " values.");
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onClearClicked() {
        if (isAnimating) return;
        heap.clear();
        drawHeap(-1, BLUE);
        updateInfoPanel();
        addLog("Cleared heap");
        setStatus("idle", "🗑  Heap cleared.");
    }

    // ─────────────────────────────────────────────────────────────
    //  SET SIZE
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onSetSizeClicked() {
        if (isAnimating) return;

        String input = maxSizeField.getText().trim();
        if (input.isEmpty()) { setStatus("warn", "⚠  Please enter a max size."); return; }

        int newSize;
        try {
            newSize = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            setStatus("warn", "⚠  Max size must be a number.");
            return;
        }

        if (newSize < 1 || newSize > 20) { setStatus("warn", "⚠  Max size must be 1–20."); return; }

        maxSize = newSize;
        while (heap.size() > maxSize) heap.remove(heap.size() - 1);

        drawHeap(-1, BLUE);
        updateInfoPanel();
        setStatus("ok", "✔  Capacity set to " + maxSize + ".");
    }

    // ─────────────────────────────────────────────────────────────
    //  TOGGLE MIN / MAX HEAP
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void onToggleHeapType() {
        if (isAnimating) return;

        isMinHeap = !isMinHeap;

        // Rebuild existing heap for the new type
        if (!heap.isEmpty()) {
            for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
                heapifyDown(i);
            }
        }

        String typeName = isMinHeap ? "Min Heap" : "Max Heap";
        toggleBtn.setText("⇅  " + typeName);
        typeLabel.setText(typeName);

        drawHeap(-1, BLUE);
        updateInfoPanel();
        addLog("Switched to " + typeName);
        setStatus("ok", "⇅  Switched to " + typeName + ".");
    }

    // ─────────────────────────────────────────────────────────────
    //  HEAPIFY UP
    //  After inserting at end, bubble the element UP
    // ─────────────────────────────────────────────────────────────
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = getParentIndex(index);

            boolean shouldSwap = isMinHeap
                    ? heap.get(index) < heap.get(parentIndex)  // MinHeap: child < parent
                    : heap.get(index) > heap.get(parentIndex); // MaxHeap: child > parent

            if (shouldSwap) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  HEAPIFY DOWN
    //  After replacing root, push element DOWN to correct position
    // ─────────────────────────────────────────────────────────────
    private void heapifyDown(int index) {
        int size = heap.size();

        while (true) {
            int left   = getLeftChildIndex(index);
            int right  = getRightChildIndex(index);
            int target = index;

            if (isMinHeap) {
                if (left  < size && heap.get(left)  < heap.get(target)) target = left;
                if (right < size && heap.get(right) < heap.get(target)) target = right;
            } else {
                if (left  < size && heap.get(left)  > heap.get(target)) target = left;
                if (right < size && heap.get(right) > heap.get(target)) target = right;
            }

            if (target != index) {
                swap(index, target);
                index = target;
            } else {
                break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  INDEX HELPERS
    // ─────────────────────────────────────────────────────────────
    private int getParentIndex(int i)     { return (i - 1) / 2; }
    private int getLeftChildIndex(int i)  { return 2 * i + 1;   }
    private int getRightChildIndex(int i) { return 2 * i + 2;   }

    private void swap(int i, int j) {
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    // ─────────────────────────────────────────────────────────────
    //  DRAW THE HEAP
    // ─────────────────────────────────────────────────────────────
    private void drawHeap(int highlightIndex, Color highlightColor) {
        heapVisualPane.getChildren().clear();

        double paneW = heapVisualPane.getWidth()  > 0 ? heapVisualPane.getWidth()  : 1000.0;
        double paneH = heapVisualPane.getHeight() > 0 ? heapVisualPane.getHeight() : 500.0;

        double cellHeight = 52;
        double cellGap    = 6;
        double cellWidth  = Math.min((paneW - 80) / maxSize - cellGap, 100);
        double totalWidth = maxSize * (cellWidth + cellGap) - cellGap;
        double startX     = (paneW - totalWidth) / 2.0;
        double cellY      = paneH / 2.0 - cellHeight / 2.0 + 30;

        for (int i = 0; i < maxSize; i++) {
            double x = startX + i * (cellWidth + cellGap);
            boolean hasValue = (i < heap.size());

            Color fillColor;
            if (!hasValue) {
                fillColor = DARK;
            } else if (i == highlightIndex) {
                fillColor = highlightColor;
            } else {
                fillColor = (highlightIndex == -1 && highlightColor == ORANGE) ? ORANGE : BLUE;
            }

            Rectangle cell = new Rectangle(x, cellY, cellWidth, cellHeight);
            cell.setFill(fillColor);
            cell.setArcWidth(8);
            cell.setArcHeight(8);
            cell.setStroke(Color.web("rgba(123,47,247,0.5)"));
            cell.setStrokeWidth(1.5);
            heapVisualPane.getChildren().add(cell);

            Text indexText = new Text(String.valueOf(i));
            indexText.setFont(Font.font("Verdana", FontWeight.NORMAL, 11));
            indexText.setFill(Color.web("#7b2ff7"));
            double iw = indexText.getLayoutBounds().getWidth();
            indexText.setX(x + (cellWidth - iw) / 2.0);
            indexText.setY(cellY + cellHeight + 18);
            heapVisualPane.getChildren().add(indexText);

            if (hasValue) {
                Text valueText = new Text(String.valueOf(heap.get(i)));
                valueText.setFont(Font.font("Verdana", FontWeight.BOLD,
                        Math.min(cellWidth * 0.35, 18)));
                valueText.setFill(Color.WHITE);
                double vw = valueText.getLayoutBounds().getWidth();
                double vh = valueText.getLayoutBounds().getHeight();
                valueText.setX(x + (cellWidth - vw) / 2.0);
                valueText.setY(cellY + cellHeight / 2.0 + vh / 4.0);
                heapVisualPane.getChildren().add(valueText);
            }
        }

        // ROOT arrow (orange) above index 0
        if (!heap.isEmpty()) {
            double rx = startX + cellWidth / 2.0;
            Rectangle shaft = new Rectangle(rx - 2, cellY - 36, 4, 28);
            shaft.setFill(ORANGE);
            heapVisualPane.getChildren().add(shaft);
            Text arrow = new Text(rx - 8, cellY - 6, "▼");
            arrow.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            arrow.setFill(ORANGE);
            heapVisualPane.getChildren().add(arrow);
            Text label = new Text("ROOT");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            label.setFill(ORANGE);
            label.setX(rx - label.getLayoutBounds().getWidth() / 2.0);
            label.setY(cellY - 42);
            heapVisualPane.getChildren().add(label);
        }

        // LAST arrow (green) above last filled index
        if (heap.size() > 1) {
            int li   = heap.size() - 1;
            double lx = startX + li * (cellWidth + cellGap) + cellWidth / 2.0;
            Rectangle shaft = new Rectangle(lx - 2, cellY - 36, 4, 28);
            shaft.setFill(GREEN);
            heapVisualPane.getChildren().add(shaft);
            Text arrow = new Text(lx - 8, cellY - 6, "▼");
            arrow.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            arrow.setFill(GREEN);
            heapVisualPane.getChildren().add(arrow);
            Text label = new Text("LAST");
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
            label.setFill(GREEN);
            label.setX(lx - label.getLayoutBounds().getWidth() / 2.0);
            label.setY(cellY - 42);
            heapVisualPane.getChildren().add(label);
        }

        // "ROOT →" on far left
        Text leftLabel = new Text("ROOT →");
        leftLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        leftLabel.setFill(Color.web("rgba(255,87,34,0.6)"));
        leftLabel.setX(startX);
        leftLabel.setY(cellY + cellHeight + 36);
        heapVisualPane.getChildren().add(leftLabel);

        // "← LAST" on far right
        double re = startX + maxSize * (cellWidth + cellGap);
        Text rightLabel = new Text("← LAST");
        rightLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        rightLabel.setFill(Color.web("rgba(102,187,106,0.6)"));
        rightLabel.setX(re - rightLabel.getLayoutBounds().getWidth() - 4);
        rightLabel.setY(cellY + cellHeight + 36);
        heapVisualPane.getChildren().add(rightLabel);
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE INFO PANEL
    // ─────────────────────────────────────────────────────────────
    private void updateInfoPanel() {
        int size = heap.size();
        sizeLabel.setText(String.valueOf(size));
        capacityLabel.setText(String.valueOf(maxSize));
        typeLabel.setText(isMinHeap ? "Min Heap" : "Max Heap");
        rootLabel.setText(size > 0 ? String.valueOf(heap.get(0)) : "—");

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
        insertBtn.setDisable(true);
        removeBtn.setDisable(true);
        peekBtn.setDisable(true);
        buildHeapBtn.setDisable(true);
        clearBtn.setDisable(true);
        toggleBtn.setDisable(true);
    }

    private void unlockButtons() {
        insertBtn.setDisable(false);
        removeBtn.setDisable(false);
        peekBtn.setDisable(false);
        buildHeapBtn.setDisable(false);
        clearBtn.setDisable(false);
        toggleBtn.setDisable(false);
    }
}