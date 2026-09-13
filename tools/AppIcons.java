package tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class AppIcons {

    public static final String PATH_TREE = "M22,11V3h-7v3H9V3H2v8h7V8h2v10h4v3h7v-8h-7v3h-2V8h2v3H22z M4,5h3v4H4V5z M17,5h3v4h-3V5z M17,15h3v4h-3V15z";

    public static final String PATH_FINGER = "M9,11.24V7.5C9,6.12 10.12,5 11.5,5S14,6.12 14,7.5v3.74c1.21,-0.81 2,-2.18 2,-3.74c0,-2.49 -2.01,-4.5 -4.5,-4.5S7,5.01 7,7.5c0,1.56 0.79,2.93 2,3.74zm9.84,4.63l-4.54,-2.26c-0.17,-0.07 -0.35,-0.11 -0.54,-0.11H13v-6c0,-0.83 -0.67,-1.5 -1.5,-1.5S10,6.67 10,7.5v10.74l-3.43,-0.72c-0.08,-0.01 -0.15,-0.02 -0.24,-0.02c-0.34,0 -0.65,0.14 -0.88,0.36L4.7,18.61l5.36,5.36c0.41,0.41 0.98,0.67 1.6,0.67h7.21c1.07,0 1.95,-0.81 2.05,-1.87l0.67,-6.13c0.03,-0.24 -0.04,-0.49 -0.19,-0.69c-0.15,-0.2 -0.37,-0.32 -0.61,-0.32h-0.95z";

    public static final String PATH_EAR = "M17,20c-0.29,0 -0.56,-0.06 -0.76,-0.15 -0.71,-0.37 -1.21,-1.07 -1.24,-1.85H15c0,-1.1 -0.9,-2 -2,-2h-0.5c-0.28,0 -0.5,0.22 -0.5,0.5v1c0,1.93 1.57,3.5 3.5,3.5c0.39,0 0.77,-0.07 1.13,-0.2 0.44,-0.17 0.87,-0.42 1.25,-0.74l-1.38,-1.38c-0.15,0.18 -0.32,0.32 -0.5,0.42zM14,4c-3.87,0 -7,3.13 -7,7v0.5c0,0.83 0.67,1.5 1.5,1.5S10,12.33 10,11.5V11c0,-2.21 1.79,-4 4,-4s4,1.79 4,4v3.5c0,0.83 -0.67,1.5 -1.5,1.5s-1.5,-0.67 -1.5,-1.5v-1c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1v1c0,1.93 1.57,3.5 3.5,3.5s3.5,-1.57 3.5,-3.5V11c0,-3.87 -3.13,-7 -7,-7z";

    public static final String PATH_HARDWARE = "M15,7v4h1v2h-3V5h2l-3,-4 -3,4h2v8H8v-2.07c0.7,-0.37 1.2,-1.08 1.2,-1.93 0,-1.21 -0.99,-2.2 -2.2,-2.2 -1.21,0 -2.2,0.99 -2.2,2.2 0,0.85 0.5,1.56 1.2,1.93V13c0,1.11 0.89,2 2,2h3v3.05c-0.71,0.37 -1.2,1.1 -1.2,1.95 0,1.22 0.99,2.2 2.2,2.2 1.21,0 2.2,-0.98 2.2,-2.2 0,-0.85 -0.49,-1.58 -1.2,-1.95V15h3c1.11,0 2,-0.89 2,-2v-2h1V7h-4z";

    public static final String PATH_SETTINGS = "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94 0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87c-0.12,0.21 -0.08,0.47 0.12,0.61l2.03,1.58c-0.05,0.3 -0.09,0.63 -0.09,0.94s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61l-2.01,-1.58zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6 3.6,1.62 3.6,3.6 -1.62,3.6 -3.6,3.6z";

    private static final Path2D SHAPE_TREE = SvgPathParser.parsePath(PATH_TREE);
    private static final Path2D SHAPE_FINGER = SvgPathParser.parsePath(PATH_FINGER);
    private static final Path2D SHAPE_EAR = SvgPathParser.parsePath(PATH_EAR);
    private static final Path2D SHAPE_HARDWARE = SvgPathParser.parsePath(PATH_HARDWARE);
    private static final Path2D SHAPE_SETTINGS = SvgPathParser.parsePath(PATH_SETTINGS);

    public static void drawIcon(Graphics2D g, int tabIndex, double cx, double cy, double size, Color color) {
        Path2D src;
        switch (tabIndex) {
            case 0: src = SHAPE_TREE; break;
            case 1: src = SHAPE_FINGER; break;
            case 2: src = SHAPE_EAR; break;
            case 3: src = SHAPE_HARDWARE; break;
            case 4: src = SHAPE_SETTINGS; break;
            default: return;
        }

        AffineTransform at = new AffineTransform();
        at.translate(cx - size / 2.0, cy - size / 2.0);
        at.scale(size / 24.0, size / 24.0);
        Shape shape = at.createTransformedShape(src);

        g.setColor(color);
        g.fill(shape);
    }
}
