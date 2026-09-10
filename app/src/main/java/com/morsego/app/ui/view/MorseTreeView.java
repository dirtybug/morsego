package com.morsego.app.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.MorseTreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom 2D Canvas View that draws the Morse Code Binary Tree interactively.
 * Highlights unlocked nodes based on user level and allows tapping nodes to hear their CW code.
 */
public class MorseTreeView extends View {

    public interface OnNodeClickListener {
        void onNodeClicked(MorseTreeNode node);
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
            return (dx * dx + dy * dy) <= (radius * radius * 1.5f);
        }
    }

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint morseTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint branchLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<RenderNode> renderNodes = new ArrayList<>();
    private OnNodeClickListener listener;
    private int currentUnlockedLevel = 1;

    public MorseTreeView(Context context) {
        super(context);
        init();
    }

    public MorseTreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MorseTreeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(Color.parseColor("#30363D"));
        linePaint.setStrokeWidth(3f);
        linePaint.setStyle(Paint.Style.STROKE);

        nodePaint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        morseTextPaint.setTextAlign(Paint.Align.CENTER);
        morseTextPaint.setColor(Color.parseColor("#8B949E"));

        branchLabelPaint.setTextAlign(Paint.Align.CENTER);
        branchLabelPaint.setTextSize(24f);
        branchLabelPaint.setFakeBoldText(true);
    }

    public void setUnlockedLevel(int level) {
        this.currentUnlockedLevel = level;
        MorseBinaryTree.getInstance().unlockNodesUpToLevel(level);
        invalidate();
    }

    public void setOnNodeClickListener(OnNodeClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        // Desired height for 4 levels
        int desiredHeight = (int) (650 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderNodes.clear();

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        MorseTreeNode root = MorseBinaryTree.getInstance().getRoot();

        float startY = 60f * getResources().getDisplayMetrics().density;
        float layerHeight = 110f * getResources().getDisplayMetrics().density;
        float radius = 22f * getResources().getDisplayMetrics().density;

        // Draw tree up to depth 3 (E, T, I, A, N, M, S, U, R, W, D, K, G, O)
        drawTreeRecursive(canvas, root, width / 2f, startY, width / 4f, layerHeight, radius, 1);
    }

    private void drawTreeRecursive(Canvas canvas, MorseTreeNode node, float x, float y,
                                   float xOffset, float layerHeight, float radius, int depth) {
        if (node == null || depth > 4) return;

        renderNodes.add(new RenderNode(node, x, y, radius));

        // Draw left child (DIT .)
        if (node.getDitChild() != null) {
            float childX = x - xOffset;
            float childY = y + layerHeight;

            linePaint.setColor(node.getDitChild().isUnlocked() ? Color.parseColor("#FFB300") : Color.parseColor("#21262D"));
            canvas.drawLine(x, y + radius, childX, childY - radius, linePaint);

            // Draw branch indicator "."
            branchLabelPaint.setColor(Color.parseColor("#FFB300"));
            canvas.drawText("•", (x + childX) / 2f - 14f, (y + childY) / 2f, branchLabelPaint);

            drawTreeRecursive(canvas, node.getDitChild(), childX, childY, xOffset / 2f, layerHeight, radius, depth + 1);
        }

        // Draw right child (DAH -)
        if (node.getDahChild() != null) {
            float childX = x + xOffset;
            float childY = y + layerHeight;

            linePaint.setColor(node.getDahChild().isUnlocked() ? Color.parseColor("#00E5FF") : Color.parseColor("#21262D"));
            canvas.drawLine(x, y + radius, childX, childY - radius, linePaint);

            // Draw branch indicator "—"
            branchLabelPaint.setColor(Color.parseColor("#00E5FF"));
            canvas.drawText("—", (x + childX) / 2f + 14f, (y + childY) / 2f, branchLabelPaint);

            drawTreeRecursive(canvas, node.getDahChild(), childX, childY, xOffset / 2f, layerHeight, radius, depth + 1);
        }

        // Draw Node Circle
        if (node.isUnlocked()) {
            nodePaint.setColor(Color.parseColor("#FFB300"));
            textPaint.setColor(Color.parseColor("#0E1117"));
        } else {
            nodePaint.setColor(Color.parseColor("#161B22"));
            textPaint.setColor(Color.parseColor("#8B949E"));
        }

        canvas.drawCircle(x, y, radius, nodePaint);

        // Draw border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f * getResources().getDisplayMetrics().density);
        borderPaint.setColor(node.isUnlocked() ? Color.parseColor("#FFE082") : Color.parseColor("#30363D"));
        canvas.drawCircle(x, y, radius, borderPaint);

        // Draw Character
        textPaint.setTextSize(radius * 0.9f);
        float textY = y - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(node.getCharacter(), x, textY, textPaint);

        // Draw Morse under node
        if (!node.getMorseCode().isEmpty()) {
            morseTextPaint.setTextSize(radius * 0.55f);
            canvas.drawText(node.getMorseCode(), x, y + radius + 18f, morseTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float tx = event.getX();
            float ty = event.getY();

            for (RenderNode rn : renderNodes) {
                if (rn.contains(tx, ty)) {
                    if (listener != null) {
                        listener.onNodeClicked(rn.node);
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
