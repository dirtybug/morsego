package tools;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class SvgPathParser {

    public static Path2D parsePath(String d) {
        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        if (d == null || d.trim().isEmpty()) return path;

        List<Token> tokens = tokenize(d);
        int idx = 0;
        double curX = 0, curY = 0;
        double startX = 0, startY = 0;
        double lastCtrlX = 0, lastCtrlY = 0;
        char lastCmd = ' ';

        while (idx < tokens.size()) {
            Token t = tokens.get(idx++);
            if (!t.isCmd) continue;
            char cmd = t.cmd;

            switch (cmd) {
                case 'M':
                case 'm': {
                    boolean first = true;
                    while (idx < tokens.size() && !tokens.get(idx).isCmd) {
                        double x = tokens.get(idx++).val;
                        double y = tokens.get(idx++).val;
                        if (cmd == 'm') {
                            curX += x;
                            curY += y;
                        } else {
                            curX = x;
                            curY = y;
                        }
                        if (first) {
                            path.moveTo(curX, curY);
                            startX = curX;
                            startY = curY;
                            first = false;
                        } else {
                            path.lineTo(curX, curY);
                        }
                    }
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                case 'L':
                case 'l': {
                    while (idx < tokens.size() && !tokens.get(idx).isCmd) {
                        double x = tokens.get(idx++).val;
                        double y = tokens.get(idx++).val;
                        if (cmd == 'l') {
                            curX += x;
                            curY += y;
                        } else {
                            curX = x;
                            curY = y;
                        }
                        path.lineTo(curX, curY);
                    }
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                case 'H':
                case 'h': {
                    while (idx < tokens.size() && !tokens.get(idx).isCmd) {
                        double x = tokens.get(idx++).val;
                        if (cmd == 'h') curX += x;
                        else curX = x;
                        path.lineTo(curX, curY);
                    }
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                case 'V':
                case 'v': {
                    while (idx < tokens.size() && !tokens.get(idx).isCmd) {
                        double y = tokens.get(idx++).val;
                        if (cmd == 'v') curY += y;
                        else curY = y;
                        path.lineTo(curX, curY);
                    }
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
                case 'C':
                case 'c': {
                    while (idx < tokens.size() && !tokens.get(idx).isCmd) {
                        double x1 = tokens.get(idx++).val;
                        double y1 = tokens.get(idx++).val;
                        double x2 = tokens.get(idx++).val;
                        double y2 = tokens.get(idx++).val;
                        double x = tokens.get(idx++).val;
                        double y = tokens.get(idx++).val;
                        if (cmd == 'c') {
                            path.curveTo(curX + x1, curY + y1, curX + x2, curY + y2, curX + x, curY + y);
                            lastCtrlX = curX + x2;
                            lastCtrlY = curY + y2;
                            curX += x;
                            curY += y;
                        } else {
                            path.curveTo(x1, y1, x2, y2, x, y);
                            lastCtrlX = x2;
                            lastCtrlY = y2;
                            curX = x;
                            curY = y;
                        }
                    }
                    break;
                }
                case 'S':
                case 's': {
                    while (idx < tokens.size() && !tokens.get(idx).isCmd) {
                        double x2 = tokens.get(idx++).val;
                        double y2 = tokens.get(idx++).val;
                        double x = tokens.get(idx++).val;
                        double y = tokens.get(idx++).val;
                        double x1, y1;
                        if (lastCmd == 'C' || lastCmd == 'c' || lastCmd == 'S' || lastCmd == 's') {
                            x1 = 2 * curX - lastCtrlX;
                            y1 = 2 * curY - lastCtrlY;
                        } else {
                            x1 = curX;
                            y1 = curY;
                        }
                        if (cmd == 's') {
                            path.curveTo(x1, y1, curX + x2, curY + y2, curX + x, curY + y);
                            lastCtrlX = curX + x2;
                            lastCtrlY = curY + y2;
                            curX += x;
                            curY += y;
                        } else {
                            path.curveTo(x1, y1, x2, y2, x, y);
                            lastCtrlX = x2;
                            lastCtrlY = y2;
                            curX = x;
                            curY = y;
                        }
                    }
                    break;
                }
                case 'Z':
                case 'z': {
                    path.closePath();
                    curX = startX;
                    curY = startY;
                    lastCtrlX = curX;
                    lastCtrlY = curY;
                    break;
                }
            }
            lastCmd = cmd;
        }

        return path;
    }

    private static List<Token> tokenize(String d) {
        List<Token> list = new ArrayList<>();
        int i = 0, n = d.length();
        while (i < n) {
            char c = d.charAt(i);
            if (Character.isWhitespace(c) || c == ',') {
                i++;
                continue;
            }
            if (isCommand(c)) {
                list.add(new Token(c));
                i++;
            } else if (c == '-' || c == '+' || c == '.' || Character.isDigit(c)) {
                int start = i;
                if (c == '-' || c == '+') i++;
                boolean hasDot = false;
                while (i < n) {
                    char ch = d.charAt(i);
                    if (Character.isDigit(ch)) {
                        i++;
                    } else if (ch == '.' && !hasDot) {
                        hasDot = true;
                        i++;
                    } else {
                        break;
                    }
                }
                double val = Double.parseDouble(d.substring(start, i));
                list.add(new Token(val));
            } else {
                i++;
            }
        }
        return list;
    }

    private static boolean isCommand(char c) {
        return "MmLlHhVvCcSsQqTtAaZz".indexOf(c) != -1;
    }

    private static class Token {
        boolean isCmd;
        char cmd;
        double val;

        Token(char cmd) {
            this.isCmd = true;
            this.cmd = cmd;
        }

        Token(double val) {
            this.isCmd = false;
            this.val = val;
        }
    }
}
