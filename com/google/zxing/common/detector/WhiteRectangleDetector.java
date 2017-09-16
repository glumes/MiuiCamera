package com.google.zxing.common.detector;

import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;

public final class WhiteRectangleDetector {
    private final int downInit;
    private final int height;
    private final BitMatrix image;
    private final int leftInit;
    private final int rightInit;
    private final int upInit;
    private final int width;

    public WhiteRectangleDetector(BitMatrix image) throws NotFoundException {
        this(image, 10, image.getWidth() / 2, image.getHeight() / 2);
    }

    public WhiteRectangleDetector(BitMatrix image, int initSize, int x, int y) throws NotFoundException {
        this.image = image;
        this.height = image.getHeight();
        this.width = image.getWidth();
        int halfsize = initSize / 2;
        this.leftInit = x - halfsize;
        this.rightInit = x + halfsize;
        this.upInit = y - halfsize;
        this.downInit = y + halfsize;
        if (this.upInit < 0 || this.leftInit < 0 || this.downInit >= this.height || this.rightInit >= this.width) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    public ResultPoint[] detect() throws NotFoundException {
        int left = this.leftInit;
        int right = this.rightInit;
        int up = this.upInit;
        int down = this.downInit;
        boolean sizeExceeded = false;
        boolean aBlackPointFoundOnBorder = true;
        boolean atLeastOneBlackPointFoundOnBorder = false;
        boolean atLeastOneBlackPointFoundOnRight = false;
        boolean atLeastOneBlackPointFoundOnBottom = false;
        boolean atLeastOneBlackPointFoundOnLeft = false;
        boolean atLeastOneBlackPointFoundOnTop = false;
        while (aBlackPointFoundOnBorder) {
            aBlackPointFoundOnBorder = false;
            boolean z = true;
            while (true) {
                if ((!z && atLeastOneBlackPointFoundOnRight) || right >= this.width) {
                } else {
                    z = containsBlackPoint(up, down, right, false);
                    if (z) {
                        right++;
                        aBlackPointFoundOnBorder = true;
                        atLeastOneBlackPointFoundOnRight = true;
                    } else if (!atLeastOneBlackPointFoundOnRight) {
                        right++;
                    }
                }
            }
            if (right >= this.width) {
                sizeExceeded = true;
                break;
            }
            boolean z2 = true;
            while (true) {
                if ((z2 || !atLeastOneBlackPointFoundOnBottom) && down < this.height) {
                    z2 = containsBlackPoint(left, right, down, true);
                    if (z2) {
                        down++;
                        aBlackPointFoundOnBorder = true;
                        atLeastOneBlackPointFoundOnBottom = true;
                    } else if (!atLeastOneBlackPointFoundOnBottom) {
                        down++;
                    }
                }
            }
            if (down >= this.height) {
                sizeExceeded = true;
                break;
            }
            boolean z3 = true;
            while (true) {
                boolean z4;
                if ((!z3 && atLeastOneBlackPointFoundOnLeft) || left < 0) {
                    if (left < 0) {
                        sizeExceeded = true;
                        break;
                    }
                    z4 = true;
                    while (true) {
                        if ((z4 && atLeastOneBlackPointFoundOnTop) || up < 0) {
                            if (up >= 0) {
                                sizeExceeded = true;
                                break;
                            } else if (!aBlackPointFoundOnBorder) {
                                atLeastOneBlackPointFoundOnBorder = true;
                            }
                        } else {
                            z4 = containsBlackPoint(left, right, up, true);
                            if (z4) {
                                up--;
                                aBlackPointFoundOnBorder = true;
                                atLeastOneBlackPointFoundOnTop = true;
                            } else if (!atLeastOneBlackPointFoundOnTop) {
                                up--;
                            }
                        }
                    }
                    if (up >= 0) {
                        sizeExceeded = true;
                        break;
                    } else if (!aBlackPointFoundOnBorder) {
                        atLeastOneBlackPointFoundOnBorder = true;
                    }
                } else {
                    z3 = containsBlackPoint(up, down, left, false);
                    if (z3) {
                        left--;
                        aBlackPointFoundOnBorder = true;
                        atLeastOneBlackPointFoundOnLeft = true;
                    } else if (!atLeastOneBlackPointFoundOnLeft) {
                        left--;
                    }
                }
            }
            if (left < 0) {
                z4 = true;
                while (true) {
                    if (!z4) {
                        break;
                    }
                }
                if (up >= 0) {
                    sizeExceeded = true;
                    break;
                } else if (!aBlackPointFoundOnBorder) {
                    atLeastOneBlackPointFoundOnBorder = true;
                }
            } else {
                sizeExceeded = true;
                break;
            }
        }
        if (!sizeExceeded && atLeastOneBlackPointFoundOnBorder) {
            int i;
            int maxSize = right - left;
            ResultPoint z5 = null;
            for (i = 1; i < maxSize; i++) {
                z5 = getBlackPointOnSegment((float) left, (float) (down - i), (float) (left + i), (float) down);
                if (z5 != null) {
                    break;
                }
            }
            if (z5 != null) {
                ResultPoint t = null;
                for (i = 1; i < maxSize; i++) {
                    t = getBlackPointOnSegment((float) left, (float) (up + i), (float) (left + i), (float) up);
                    if (t != null) {
                        break;
                    }
                }
                if (t != null) {
                    ResultPoint x = null;
                    for (i = 1; i < maxSize; i++) {
                        x = getBlackPointOnSegment((float) right, (float) (up + i), (float) (right - i), (float) up);
                        if (x != null) {
                            break;
                        }
                    }
                    if (x != null) {
                        ResultPoint y = null;
                        for (i = 1; i < maxSize; i++) {
                            y = getBlackPointOnSegment((float) right, (float) (down - i), (float) (right - i), (float) down);
                            if (y != null) {
                                break;
                            }
                        }
                        if (y != null) {
                            return centerEdges(y, z5, x, t);
                        }
                        throw NotFoundException.getNotFoundInstance();
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
                throw NotFoundException.getNotFoundInstance();
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private ResultPoint getBlackPointOnSegment(float aX, float aY, float bX, float bY) {
        int dist = MathUtils.round(MathUtils.distance(aX, aY, bX, bY));
        float xStep = (bX - aX) / ((float) dist);
        float yStep = (bY - aY) / ((float) dist);
        for (int i = 0; i < dist; i++) {
            int x = MathUtils.round((((float) i) * xStep) + aX);
            int y = MathUtils.round((((float) i) * yStep) + aY);
            if (this.image.get(x, y)) {
                return new ResultPoint((float) x, (float) y);
            }
        }
        return null;
    }

    private ResultPoint[] centerEdges(ResultPoint y, ResultPoint z, ResultPoint x, ResultPoint t) {
        float yi = y.getX();
        float yj = y.getY();
        float zi = z.getX();
        float zj = z.getY();
        float xi = x.getX();
        float xj = x.getY();
        float ti = t.getX();
        float tj = t.getY();
        if (yi < ((float) this.width) / 2.0f) {
            return new ResultPoint[]{new ResultPoint(ti - 1.0f, 1.0f + tj), new ResultPoint(1.0f + zi, 1.0f + zj), new ResultPoint(xi - 1.0f, xj - 1.0f), new ResultPoint(1.0f + yi, yj - 1.0f)};
        }
        return new ResultPoint[]{new ResultPoint(1.0f + ti, 1.0f + tj), new ResultPoint(1.0f + zi, zj - 1.0f), new ResultPoint(xi - 1.0f, 1.0f + xj), new ResultPoint(yi - 1.0f, yj - 1.0f)};
    }

    private boolean containsBlackPoint(int a, int b, int fixed, boolean horizontal) {
        if (horizontal) {
            for (int x = a; x <= b; x++) {
                if (this.image.get(x, fixed)) {
                    return true;
                }
            }
        } else {
            for (int y = a; y <= b; y++) {
                if (this.image.get(fixed, y)) {
                    return true;
                }
            }
        }
        return false;
    }
}