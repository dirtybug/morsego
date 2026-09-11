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
        float density = getResources().getDisplayMetrics().density;
        int orientation = getResources().getConfiguration().orientation;

        // Adaptive dimensions for Portrait and Landscape orientations
        int desiredHeight = (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
                ? (int) (520 * density)
                : (int) (600 * density);
        int desiredWidth = Math.max(width, (int) (980 * density));

        setMeasuredDimension(desiredWidth, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderNodes.clear();

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        MorseTreeNode root = MorseBinaryTree.getInstance().getRoot();

        float density = getResources().getDisplayMetrics().density;
        float startY = 48f * density;
        float layerHeight = 105f * density;
        float radius = 22f * density;

        // Draw full tree up to depth 5 (All 26 letters A-Z, including X and B under D, C and Y under K, Z and Q under G)
        drawTreeRecursive(canvas, root, width / 2f, startY, width / 4f, layerHeight, radius, 1, density);
    }

    private void drawTreeRecursive(Canvas canvas, MorseTreeNode node, float x, float y,
                                   float xOffset, float layerHeight, float baseRadius, int depth, float density) {
        if (node == null || depth > 5) return;

        // Adaptive radius so deeper levels never overlap horizontally
        float currentRadius = (depth >= 5) ? (16f * density) : (depth == 4 ? (18f * density) : baseRadius);
        renderNodes.add(new RenderNode(node, x, y, currentRadius));

        float branchOffset = 15f * density;
        float textCenterOffsetY = (branchLabelPaint.descent() + branchLabelPaint.ascent()) / 2f;

        // Draw left child (DAH — : e.g. T under Root, M under T, A under E, X under D)
        if (node.getDahChild() != null) {
            float childX = x - xOffset;
            float childY = y + layerHeight;
            float childRadius = (depth + 1 >= 5) ? (16f * density) : (depth + 1 == 4 ? (18f * density) : baseRadius);

            linePaint.setColor(node.getDahChild().isUnlocked() ? Color.parseColor("#00E5FF") : Color.parseColor("#21262D"));
            canvas.drawLine(x, y + currentRadius, childX, childY - childRadius, linePaint);

            // Draw branch indicator "—" safely beside the line without covering it
            branchLabelPaint.setColor(Color.parseColor("#00E5FF"));
            float branchY = (y + childY) / 2f - textCenterOffsetY;
            canvas.drawText("—", (x + childX) / 2f - branchOffset, branchY, branchLabelPaint);

            drawTreeRecursive(canvas, node.getDahChild(), childX, childY, xOffset / 2f, layerHeight, baseRadius, depth + 1, density);
        }

        // Draw right child (DIT • : e.g. E under Root, N under T, I under E, B under D)
        if (node.getDitChild() != null) {
            float childX = x + xOffset;
            float childY = y + layerHeight;
            float childRadius = (depth + 1 >= 5) ? (16f * density) : (depth + 1 == 4 ? (18f * density) : baseRadius);

            linePaint.setColor(node.getDitChild().isUnlocked() ? Color.parseColor("#FFB300") : Color.parseColor("#21262D"));
            canvas.drawLine(x, y + currentRadius, childX, childY - childRadius, linePaint);

            // Draw branch indicator "•" safely beside the line without covering it
            branchLabelPaint.setColor(Color.parseColor("#FFB300"));
            float branchY = (y + childY) / 2f - textCenterOffsetY;
            canvas.drawText("•", (x + childX) / 2f + branchOffset, branchY, branchLabelPaint);

            drawTreeRecursive(canvas, node.getDitChild(), childX, childY, xOffset / 2f, layerHeight, baseRadius, depth + 1, density);
        }

        // Draw Node Circle
        if (node.isUnlocked()) {
            nodePaint.setColor(Color.parseColor("#FFB300"));
            textPaint.setColor(Color.parseColor("#0E1117"));
        } else {
            nodePaint.setColor(Color.parseColor("#161B22"));
            textPaint.setColor(Color.parseColor("#8B949E"));
        }

        canvas.drawCircle(x, y, currentRadius, nodePaint);

        // Draw border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f * density);
        borderPaint.setColor(node.isUnlocked() ? Color.parseColor("#FFE082") : Color.parseColor("#30363D"));
        canvas.drawCircle(x, y, currentRadius, borderPaint);

        // Draw Character precisely centered in circle
        textPaint.setTextSize(currentRadius * 0.95f);
        float textY = y - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(node.getCharacter(), x, textY, textPaint);

        // Draw Morse code strictly BELOW circle with guaranteed clearance (never overlaps circle border)
        if (!node.getMorseCode().isEmpty()) {
            morseTextPaint.setTextSize(currentRadius * 0.52f);
            float morseY = y + currentRadius + (12f * density);
            canvas.drawText(node.getMorseCode(), x, morseY, morseTextPaint);
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
