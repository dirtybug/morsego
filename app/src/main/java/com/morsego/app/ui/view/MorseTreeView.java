package com.morsego.app.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.MorseTreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom 2D Canvas View that renders an interactive, compact Morse Code Binary Tree.
 * Features:
 * - Compact layout: reduced node radii and layer heights so the whole tree is easily visible.
 * - Multi-touch Pinch-to-Zoom and smooth 2D Drag-to-Pan navigation.
 * - Double-tap to toggle between full overview (fit) and detail zoom.
 * - Programmatic Zoom Controls (+, −, Reset).
 * - Tap to hear CW code and trace binary path.
 */
public class MorseTreeView extends View {

    public interface OnNodeClickListener {
        void onNodeClicked(MorseTreeNode node);
    }

    public interface OnZoomChangeListener {
        void onZoomChanged(float scaleFactor);
    }

    private static class RenderNode {
        MorseTreeNode node;
        float x;
        float y;
        float radius;

        RenderNode(MorseTreeNode node, float x, float y, float radius) {
            this.node = node;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        boolean contains(float px, float py) {
            float dx = px - x;
            float dy = py - y;
            return (dx * dx + dy * dy) <= (radius * radius * 1.8f);
        }
    }

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint morseTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint branchLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<RenderNode> renderNodes = new ArrayList<>();
    private OnNodeClickListener listener;
    private OnZoomChangeListener zoomChangeListener;
    private int currentUnlockedLevel = 1;

    // Zoom and Pan state
    public static final float MIN_SCALE = 0.45f;
    public static final float MAX_SCALE = 2.8f;
    private float scaleFactor = 0.85f;
    private float defaultFitScale = 0.85f;
    private float translateX = 0f;
    private float translateY = 0f;

    // Touch gesture tracking
    private ScaleGestureDetector scaleDetector;
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;
    private float touchDownX = 0f;
    private float touchDownY = 0f;
    private long touchDownTime = 0;
    private long lastTapTime = 0;
    private boolean isDragging = false;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private boolean initializedLayout = false;

    public MorseTreeView(Context context) {
        super(context);
        init(context);
    }

    public MorseTreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MorseTreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        linePaint.setColor(Color.parseColor("#30363D"));
        linePaint.setStrokeWidth(2.5f);
        linePaint.setStyle(Paint.Style.STROKE);

        nodePaint.setStyle(Paint.Style.FILL);

        borderPaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        morseTextPaint.setTextAlign(Paint.Align.CENTER);
        morseTextPaint.setColor(Color.parseColor("#8B949E"));

        branchLabelPaint.setTextAlign(Paint.Align.CENTER);
        branchLabelPaint.setFakeBoldText(true);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setUnlockedLevel(int level) {
        this.currentUnlockedLevel = level;
        MorseBinaryTree.getInstance().unlockNodesUpToLevel(level);
        invalidate();
    }

    public void setOnNodeClickListener(OnNodeClickListener listener) {
        this.listener = listener;
    }

    public void setOnZoomChangeListener(OnZoomChangeListener listener) {
        this.zoomChangeListener = listener;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void zoomIn() {
        setScale(scaleFactor + 0.20f);
    }

    public void zoomOut() {
        setScale(scaleFactor - 0.20f);
    }

    public void resetZoom() {
        scaleFactor = defaultFitScale;
        centerTreeContent();
        invalidate();
        if (zoomChangeListener != null) {
            zoomChangeListener.onZoomChanged(scaleFactor);
        }
    }

    public void setScale(float newScale) {
        float oldScale = scaleFactor;
        scaleFactor = Math.max(MIN_SCALE, Math.min(newScale, MAX_SCALE));

        // Zoom centered on the viewport
        float viewCenterX = getWidth() / 2f;
        float viewCenterY = getHeight() / 2f;
        translateX += (viewCenterX - translateX) * (1f - scaleFactor / oldScale);
        translateY += (viewCenterY - translateY) * (1f - scaleFactor / oldScale);

        clampTranslation();
        invalidate();
        if (zoomChangeListener != null) {
            zoomChangeListener.onZoomChanged(scaleFactor);
        }
    }

    private float getVirtualTreeWidth() {
        float density = getResources().getDisplayMetrics().density;
        return 720f * density;
    }

    private float getVirtualTreeHeight() {
        float density = getResources().getDisplayMetrics().density;
        // 36dp (startY) + 4 * 66dp (layers) + 14dp (leaf radius) + 16dp (morse) = ~330dp
        return 335f * density;
    }

    private void centerTreeContent() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float virtualWidth = getVirtualTreeWidth() * scaleFactor;
        float virtualHeight = getVirtualTreeHeight() * scaleFactor;

        translateX = (viewWidth - virtualWidth) / 2f;
        if (viewHeight > virtualHeight) {
            translateY = Math.max(12f, (viewHeight - virtualHeight) / 3f);
        } else {
            translateY = 12f;
        }
    }

    private void clampTranslation() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float virtualWidth = getVirtualTreeWidth() * scaleFactor;
        float virtualHeight = getVirtualTreeHeight() * scaleFactor;

        float maxOverScrollX = Math.max(viewWidth * 0.4f, 80f);
        float maxOverScrollY = Math.max(viewHeight * 0.4f, 80f);

        float minX = viewWidth - virtualWidth - maxOverScrollX;
        float maxX = maxOverScrollX;
        if (minX > maxX) {
            translateX = (viewWidth - virtualWidth) / 2f;
        } else {
            translateX = Math.max(minX, Math.min(translateX, maxX));
        }

        float minY = viewHeight - virtualHeight - maxOverScrollY;
        float maxY = maxOverScrollY;
        if (minY > maxY) {
            translateY = Math.max(8f, (viewHeight - virtualHeight) / 3f);
        } else {
            translateY = Math.max(minY, Math.min(translateY, maxY));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float density = getResources().getDisplayMetrics().density;
        int orientation = getResources().getConfiguration().orientation;

        // Compact adaptive dimensions: much smaller than before to fit mobile screens
        int desiredHeight = (orientation == Configuration.ORIENTATION_LANDSCAPE)
                ? (int) (290 * density)
                : (int) (350 * density);

        int measuredWidth = (width > 0) ? width : (int) (getVirtualTreeWidth());
        setMeasuredDimension(measuredWidth, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        // Calculate auto-fit scale so the entire tree fits comfortably on screen
        float virtualWidth = getVirtualTreeWidth();
        float fitWidthScale = (w * 0.94f) / virtualWidth;
        defaultFitScale = Math.max(MIN_SCALE, Math.min(fitWidthScale, 1.0f));

        if (!initializedLayout) {
            scaleFactor = defaultFitScale;
            centerTreeContent();
            initializedLayout = true;
            if (zoomChangeListener != null) {
                zoomChangeListener.onZoomChanged(scaleFactor);
            }
        } else {
            clampTranslation();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderNodes.clear();

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);

        MorseTreeNode root = MorseBinaryTree.getInstance().getRoot();
        float density = getResources().getDisplayMetrics().density;
        float virtualWidth = getVirtualTreeWidth();

        // Compact base geometry: smaller nodes and reduced layer heights
        float startY = 36f * density;
        float layerHeight = 66f * density;
        float baseRadius = 15.5f * density;

        branchLabelPaint.setTextSize(16f * density);
        borderPaint.setStrokeWidth(2f * density);

        // Draw full binary tree recursively up to depth 5 (all 26 letters A-Z)
        drawTreeRecursive(canvas, root, virtualWidth / 2f, startY, virtualWidth / 4f, layerHeight, baseRadius, 1, density);

        canvas.restore();
    }

    private void drawTreeRecursive(Canvas canvas, MorseTreeNode node, float x, float y,
                                   float xOffset, float layerHeight, float baseRadius, int depth, float density) {
        if (node == null || depth > 5) return;

        // Adaptive node radius: compact hierarchy preventing horizontal collisions at depth 5
        float currentRadius;
        if (depth >= 5) {
            currentRadius = 11.5f * density;
        } else if (depth == 4) {
            currentRadius = 13.0f * density;
        } else if (depth == 3) {
            currentRadius = 14.5f * density;
        } else {
            currentRadius = baseRadius;
        }

        renderNodes.add(new RenderNode(node, x, y, currentRadius));

        float branchOffset = 10.5f * density;
        float textCenterOffsetY = (branchLabelPaint.descent() + branchLabelPaint.ascent()) / 2f;

        // Draw left child (DAH — : e.g. T under Root, M under T, A under E, X under D)
        if (node.getDahChild() != null) {
            float childX = x - xOffset;
            float childY = y + layerHeight;
            float childRadius = (depth + 1 >= 5) ? (11.5f * density) : ((depth + 1 == 4) ? 13.0f * density : baseRadius);

            linePaint.setColor(node.getDahChild().isUnlocked() ? Color.parseColor("#00E5FF") : Color.parseColor("#21262D"));
            canvas.drawLine(x, y + currentRadius, childX, childY - childRadius, linePaint);

            // Draw branch indicator "—" safely beside connecting line
            branchLabelPaint.setColor(Color.parseColor("#00E5FF"));
            float branchY = (y + childY) / 2f - textCenterOffsetY;
            canvas.drawText("—", (x + childX) / 2f - branchOffset, branchY, branchLabelPaint);

            drawTreeRecursive(canvas, node.getDahChild(), childX, childY, xOffset / 2f, layerHeight, baseRadius, depth + 1, density);
        }

        // Draw right child (DIT • : e.g. E under Root, N under T, I under E, B under D)
        if (node.getDitChild() != null) {
            float childX = x + xOffset;
            float childY = y + layerHeight;
            float childRadius = (depth + 1 >= 5) ? (11.5f * density) : ((depth + 1 == 4) ? 13.0f * density : baseRadius);

            linePaint.setColor(node.getDitChild().isUnlocked() ? Color.parseColor("#FFB300") : Color.parseColor("#21262D"));
            canvas.drawLine(x, y + currentRadius, childX, childY - childRadius, linePaint);

            // Draw branch indicator "•" safely beside connecting line
            branchLabelPaint.setColor(Color.parseColor("#FFB300"));
            float branchY = (y + childY) / 2f - textCenterOffsetY;
            canvas.drawText("•", (x + childX) / 2f + branchOffset, branchY, branchLabelPaint);

            drawTreeRecursive(canvas, node.getDitChild(), childX, childY, xOffset / 2f, layerHeight, baseRadius, depth + 1, density);
        }

        // Draw Node Circle
        if (node.isUnlocked()) {
            nodePaint.setColor(Color.parseColor("#FFB300"));
            textPaint.setColor(Color.parseColor("#0E1117"));
            borderPaint.setColor(Color.parseColor("#FFE082"));
        } else {
            nodePaint.setColor(Color.parseColor("#161B22"));
            textPaint.setColor(Color.parseColor("#8B949E"));
            borderPaint.setColor(Color.parseColor("#30363D"));
        }

        canvas.drawCircle(x, y, currentRadius, nodePaint);
        canvas.drawCircle(x, y, currentRadius, borderPaint);

        // Draw Character centered in circle
        textPaint.setTextSize(currentRadius * 0.95f);
        float textY = y - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(node.getCharacter(), x, textY, textPaint);

        // Draw Morse code strictly below circle with clean spacing
        if (!node.getMorseCode().isEmpty()) {
            morseTextPaint.setTextSize(currentRadius * 0.55f);
            float morseY = y + currentRadius + (10.5f * density);
            canvas.drawText(node.getMorseCode(), x, morseY, morseTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                touchDownTime = System.currentTimeMillis();
                touchDownX = event.getX();
                touchDownY = event.getY();
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                activePointerId = event.getPointerId(0);
                isDragging = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex != -1) {
                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    float distFromDown = (float) Math.hypot(x - touchDownX, y - touchDownY);
                    if (distFromDown > 12f || scaleDetector.isInProgress()) {
                        isDragging = true;
                        if (getParent() != null) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }

                    if (isDragging && !scaleDetector.isInProgress()) {
                        translateX += dx;
                        translateY += dy;
                        clampTranslation();
                        invalidate();
                    }

                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                long duration = System.currentTimeMillis() - touchDownTime;
                float distFromDown = (float) Math.hypot(event.getX() - touchDownX, event.getY() - touchDownY);

                if (!isDragging && distFromDown < 16f && duration < 380) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTapTime < 320) {
                        // Double tap: toggle zoom between fit and detailed zoom (1.35x)
                        if (Math.abs(scaleFactor - defaultFitScale) < 0.15f) {
                            setScale(1.35f);
                        } else {
                            resetZoom();
                        }
                        lastTapTime = 0;
                    } else {
                        lastTapTime = currentTime;
                        handleTap(event.getX(), event.getY());
                    }
                }

                activePointerId = MotionEvent.INVALID_POINTER_ID;
                isDragging = false;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                isDragging = false;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    int newPointerIndex = (pointerIndex == 0) ? 1 : 0;
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private void handleTap(float screenX, float screenY) {
        float worldX = (screenX - translateX) / scaleFactor;
        float worldY = (screenY - translateY) / scaleFactor;

        for (RenderNode rn : renderNodes) {
            if (rn.contains(worldX, worldY)) {
                if (listener != null) {
                    listener.onNodeClicked(rn.node);
                }
                return;
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float previousScale = scaleFactor;
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

            // Zoom centered on the pinch focus point
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            translateX += (focusX - translateX) * (1f - scaleFactor / previousScale);
            translateY += (focusY - translateY) * (1f - scaleFactor / previousScale);

            clampTranslation();
            invalidate();
            if (zoomChangeListener != null) {
                zoomChangeListener.onZoomChanged(scaleFactor);
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }
    }
}
