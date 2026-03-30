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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Search_Controller {

    /* Sidebar */
    @FXML private Button home_btn;
    @FXML private Button linear_search_btn;
    @FXML private Button binary_search_btn;

    /* Top bar */
    @FXML private Label     activeAlgoLabel;
    @FXML private TextField targetField;
    @FXML private TextField arraySizeField;

    /* Playback controls */
    @FXML private Button pauseBtn;
    @FXML private Button stepBackBtn;
    @FXML private Button stepFwdBtn;
    @FXML private Label  stepCounterLabel;

    /* Tab pane */
    @FXML private TabPane searchTabPane;
    @FXML private Tab     linearTab;
    @FXML private Tab     binaryTab;

    /* Linear tab */
    @FXML private Pane  linearVisualPane;
    @FXML private Label linearStepLabel;

    /* Binary tab */
    @FXML private Pane  binaryVisualPane;
    @FXML private Label binaryStepLabel;
    @FXML private Label loLabel;
    @FXML private Label midLabel;
    @FXML private Label hiLabel;


    private int[] array;
    private int[] sortedArray;
    private int   target;

    private List<int[]> linearSteps = new ArrayList<>();
    private List<int[]> binarySteps = new ArrayList<>();

    private int currentStep = 0;

    private Timeline timeline;

    private static final double PLAY_DELAY_MS = 600.0;


    private boolean isPaused = false;

    private static final Color C_DEFAULT = Color.web("#4FC3F7");
    private static final Color C_CURRENT = Color.web("#FFD54F");
    private static final Color C_DEAD    = Color.web("#EF5350");
    private static final Color C_FOUND   = Color.web("#66BB6A");
    private static final Color C_RANGE   = Color.web("#CE93D8");

    private static final double GAP          = 5.0;
    private static final double LABEL_MARGIN = 52.0;



    @FXML
    public void initialize() {
        generateArray(20);
        highlightSidebarButton("linear");
        resetPointerLabels();
        setAllControlsIdle();
    }


    @FXML
    private void onBackClicked() {
        stopTimeline();
        SceneManager.switchTo("menu_scene");
    }

    @FXML
    private void load_linear_search() {
        stopTimeline();
        searchTabPane.getSelectionModel().select(linearTab);
        activeAlgoLabel.setText("Linear Search");
        highlightSidebarButton("linear");
        clearAll();
    }

    @FXML
    private void load_binary_search() {
        stopTimeline();
        searchTabPane.getSelectionModel().select(binaryTab);
        activeAlgoLabel.setText("Binary Search");
        highlightSidebarButton("binary");
        resetPointerLabels();
        clearAll();
    }

    @FXML
    private void onSearchClicked() {
        stopTimeline();

        // Validate target
        String tText = targetField.getText().trim();

        try {
            target = Integer.parseInt(tText);
        } catch (NumberFormatException e) {
            return;
        }

        String sText = arraySizeField.getText().trim();
        if (!sText.isEmpty()) {
            try {
                int sz = Integer.parseInt(sText);

                generateArray(sz);
            } catch (NumberFormatException e) {

                return;
            }
        }

        boolean isLinear = searchTabPane.getSelectionModel().getSelectedItem() == linearTab;
        if (isLinear) prepareLinearSearch();
        else          prepareBinarySearch();

        startAutoPlay();
    }

    @FXML
    private void onResetClicked() {
        stopTimeline();
        int sz = 20;
        String sText = arraySizeField.getText().trim();
        if (!sText.isEmpty()) {
            try { sz = Math.max(2, Math.min(50, Integer.parseInt(sText))); }
            catch (NumberFormatException ignored) {}
        }
        generateArray(sz);
        clearAll();
        resetPointerLabels();
        linearStepLabel.setText("Step: 0 / 0");
        binaryStepLabel.setText("Step: 0 / 0");
    }
    @FXML
    private void onPauseClicked() {
        if (timeline == null) return;

        if (!isPaused) {
            // ── Pause ────────────────────────────────────────────
            timeline.pause();
            isPaused = true;
            pauseBtn.setText("▶  Resume");
            pauseBtn.setStyle(pauseBtn.getStyle().replace(
                    "linear-gradient(to bottom right, #f57f17, #e65100)",
                    "linear-gradient(to bottom right, #00c853, #007a2f)"));
            enableStepButtons();


        } else {
            // ── Resume ───────────────────────────────────────────
            timeline.play();
            isPaused = false;
            pauseBtn.setText("⏸  Pause");
            pauseBtn.setStyle(pauseBtn.getStyle().replace(
                    "linear-gradient(to bottom right, #00c853, #007a2f)",
                    "linear-gradient(to bottom right, #f57f17, #e65100)"));
            disableStepButtons();

        }
    }


    @FXML
    private void onStepBack() {
        if (!isPaused) return;   // guard — should never fire when running
        if (currentStep <= 0) return;
        currentStep--;
        renderStep(currentStep);
        refreshStepCounter();
    }

    @FXML
    private void onStepForward() {
        if (!isPaused) return;   // guard
        List<int[]> steps = activeSteps();
        if (currentStep >= steps.size() - 1) return;
        currentStep++;
        renderStep(currentStep);
        refreshStepCounter();
    }

    private void generateArray(int size) {
        List<Integer> pool = new ArrayList<>();
        for (int i = 1; i <= 99; i++) pool.add(i);
        Collections.shuffle(pool);

        array = new int[size];
        for (int i = 0; i < size; i++) array[i] = pool.get(i);

        sortedArray = array.clone();
        Arrays.sort(sortedArray);

        drawBars(linearVisualPane, array,       new int[0], -1, -1, -1, -1);
        drawBars(binaryVisualPane, sortedArray, new int[0], -1, -1, -1, -1);
    }

    private void prepareLinearSearch() {
        linearSteps.clear();
        currentStep = 0;
        int n = array.length;
        List<Integer> dead = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            boolean hit = (array[i] == target);
            linearSteps.add(buildSnap(i, hit ? i : -1, -1, -1, dead));
            if (hit) break;
            dead.add(i);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  BUILD SNAPSHOTS — BINARY
    // ════════════════════════════════════════════════════════════

    private void prepareBinarySearch() {
        binarySteps.clear();
        currentStep = 0;
        int n = sortedArray.length;
        int lo = 0, hi = n - 1;

        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            boolean hit = (sortedArray[mid] == target);

            List<Integer> dead = new ArrayList<>();
            for (int i = 0;    i < lo; i++) dead.add(i);
            for (int i = hi+1; i < n;  i++) dead.add(i);

            binarySteps.add(buildSnap(mid, hit ? mid : -1, lo, hi, dead));
            if (hit) break;
            if (sortedArray[mid] < target) lo = mid + 1;
            else                           hi = mid - 1;
        }
    }

    private void startAutoPlay() {
        List<int[]> steps = activeSteps();
        if (steps.isEmpty()) return;

        isPaused = false;
        currentStep = 0;

        // Pause btn becomes active, step buttons stay locked
        pauseBtn.setDisable(false);
        pauseBtn.setText("⏸  Pause");
        disableStepButtons();

        stepCounterLabel.setText("1 / " + steps.size());

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(PLAY_DELAY_MS), event -> {
                    renderStep(currentStep);
                    refreshStepCounter();

                    if (currentStep >= steps.size() - 1) {
                        // Last step reached — stop and hand control to user
                        onSearchComplete();
                    } else {
                        currentStep++;
                    }
                })
        );

        timeline.play();
    }

    private void onSearchComplete() {
        stopTimeline();
        isPaused = true;   // treat as "paused" so step buttons work
        pauseBtn.setDisable(true);
        pauseBtn.setText("⏸  Pause");
        enableStepButtons();
    }

    private void renderStep(int stepIndex) {
        boolean isLinear = searchTabPane.getSelectionModel().getSelectedItem() == linearTab;
        List<int[]> steps = activeSteps();
        int total = steps.size();

        if (steps.isEmpty() || stepIndex < 0 || stepIndex >= total) return;

        int[] snap     = steps.get(stepIndex);
        int   curIdx   = snap[0];
        int   foundIdx = snap[1];
        int   lo       = snap[2];
        int   hi       = snap[3];
        int[] dead     = Arrays.copyOfRange(snap, 4, snap.length);

        if (isLinear) {
            drawBars(linearVisualPane, array, dead, curIdx, foundIdx, -1, -1);
            linearStepLabel.setText("Step: " + (stepIndex + 1) + " / " + total);



        } else {
            drawBars(binaryVisualPane, sortedArray, dead, curIdx, foundIdx, lo, hi);
            binaryStepLabel.setText("Step: " + (stepIndex + 1) + " / " + total);

            loLabel.setText("lo=" + lo);
            midLabel.setText("mid=" + curIdx + " (" + sortedArray[curIdx] + ")");
            hiLabel.setText("hi=" + hi);


        }
    }


    private void drawBars(Pane pane, int[] arr, int[] deadIdxs,
                          int curIdx, int foundIdx, int lo, int hi) {
        pane.getChildren().clear();

        double paneW = pane.getWidth()  > 0 ? pane.getWidth()  : 1230.0;
        double paneH = pane.getHeight() > 0 ? pane.getHeight() : 540.0;

        int    n      = arr.length;
        double barW   = (paneW - GAP * (n + 1)) / n;
        int    maxVal = Arrays.stream(arr).max().orElse(1);
        boolean[] isDead = markSet(n, deadIdxs);

        for (int i = 0; i < n; i++) {
            double barH = (arr[i] / (double) maxVal) * (paneH - LABEL_MARGIN);
            double x    = GAP + i * (barW + GAP);
            double y    = paneH - barH - LABEL_MARGIN;

            Color color;
            if      (i == foundIdx)                      color = C_FOUND;
            else if (i == curIdx)                        color = C_CURRENT;
            else if (isDead[i])                          color = C_DEAD;
            else if (lo >= 0 && hi >= 0
                    && i >= lo && i <= hi)              color = C_RANGE;
            else                                         color = C_DEFAULT;

            Rectangle rect = new Rectangle(x, y, barW, barH);
            rect.setFill(color);
            rect.setArcWidth(5);
            rect.setArcHeight(5);

            if (i == curIdx || i == foundIdx) {
                String g = (i == foundIdx)
                        ? "rgba(102,187,106,0.8)" : "rgba(255,213,79,0.8)";
                rect.setStyle("-fx-effect: dropshadow(gaussian," + g + ",14,0.5,0,0);");
            }

            pane.getChildren().add(rect);

            if (barW >= 14) {
                Text val = new Text(String.valueOf(arr[i]));
                val.setFont(Font.font("Verdana", FontWeight.BOLD, Math.min(barW * 0.44, 13)));
                val.setFill(Color.WHITE);
                double tw = val.getLayoutBounds().getWidth();
                val.setX(x + (barW - tw) / 2.0);
                val.setY(y - 5);
                pane.getChildren().add(val);
            }

            if (barW >= 12) {
                Text idxT = new Text(String.valueOf(i));
                idxT.setFont(Font.font("Verdana", FontWeight.NORMAL, Math.min(barW * 0.36, 11)));
                idxT.setFill(Color.web("#7b2ff7"));
                double tw = idxT.getLayoutBounds().getWidth();
                idxT.setX(x + (barW - tw) / 2.0);
                idxT.setY(paneH - LABEL_MARGIN + 16);
                pane.getChildren().add(idxT);
            }
        }
    }



    private int[] buildSnap(int curIdx, int foundIdx, int lo, int hi,
                            List<Integer> dead) {
        int[] snap = new int[4 + dead.size()];
        snap[0] = curIdx;
        snap[1] = foundIdx;
        snap[2] = lo;
        snap[3] = hi;
        for (int i = 0; i < dead.size(); i++) snap[4 + i] = dead.get(i);
        return snap;
    }

    private List<int[]> activeSteps() {
        boolean isLinear = searchTabPane.getSelectionModel().getSelectedItem() == linearTab;
        return isLinear ? linearSteps : binarySteps;
    }

    private void stopTimeline() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        isPaused = false;
    }

    private void clearAll() {
        stopTimeline();
        linearSteps.clear();
        binarySteps.clear();
        currentStep = 0;
        setAllControlsIdle();
    }

    private void setAllControlsIdle() {
        pauseBtn.setDisable(true);
        pauseBtn.setText("⏸  Pause");
        disableStepButtons();
        stepCounterLabel.setText("0 / 0");

    }

    private void enableStepButtons() {
        List<int[]> steps = activeSteps();
        int total = steps.size();
        boolean atStart = (currentStep <= 0);
        boolean atEnd   = (currentStep >= total - 1);

        stepBackBtn.setDisable(atStart);
        stepFwdBtn.setDisable(atEnd);
        stepBackBtn.setOpacity(atStart ? 0.4 : 1.0);
        stepFwdBtn.setOpacity(atEnd   ? 0.4 : 1.0);
    }

    private void disableStepButtons() {
        stepBackBtn.setDisable(true);
        stepFwdBtn.setDisable(true);
        stepBackBtn.setOpacity(0.4);
        stepFwdBtn.setOpacity(0.4);
    }

    private void refreshStepCounter() {
        List<int[]> steps = activeSteps();
        int total = steps.size();
        stepCounterLabel.setText(total == 0 ? "0 / 0"
                : (currentStep + 1) + " / " + total);

        if (isPaused) enableStepButtons();
    }

    private boolean[] markSet(int n, int[] indices) {
        boolean[] r = new boolean[n];
        for (int i : indices) if (i >= 0 && i < n) r[i] = true;
        return r;
    }

    private void resetPointerLabels() {
        loLabel.setText("lo=—");
        midLabel.setText("mid=—");
        hiLabel.setText("hi=—");
    }

    private void highlightSidebarButton(String algo) {
        linear_search_btn.setOpacity("linear".equals(algo) ? 1.0 : 0.45);
        binary_search_btn.setOpacity("binary".equals(algo) ? 1.0 : 0.45);
    }
}