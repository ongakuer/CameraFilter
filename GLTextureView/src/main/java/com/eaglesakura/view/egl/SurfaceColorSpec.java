package com.eaglesakura.view.egl;

/**
 * EGLで利用する色バッファ情報
 */
public enum SurfaceColorSpec {

    /**
     * RGBA各8bit
     */
    RGBA8 {
        @Override
        public int getRedSize() {
            return 8;
        }

        @Override
        public int getGreenSize() {
            return 8;
        }

        @Override
        public int getBlueSize() {
            return 8;
        }

        @Override
        public int getAlphaSize() {
            return 8;
        }
    },

    /**
     * RGB各8bit
     */
    RGB8 {
        @Override
        public int getRedSize() {
            return 8;
        }

        @Override
        public int getGreenSize() {
            return 8;
        }

        @Override
        public int getBlueSize() {
            return 8;
        }

        @Override
        public int getAlphaSize() {
            return 0;
        }
    },

    /**
     * RGB各5/6/5bit
     */
    RGB565 {
        @Override
        public int getAlphaSize() {
            return 5;
        }

        @Override
        public int getGreenSize() {
            return 6;
        }

        @Override
        public int getBlueSize() {
            return 5;
        }

        @Override
        public int getRedSize() {
            return 0;
        }
    };

    /**
     * R bits
     * @return
     */
    public abstract int getRedSize();

    /**
     * B bits
     * @return
     */
    public abstract int getBlueSize();

    /**
     * G bits
     * @return
     */
    public abstract int getGreenSize();

    /**
     * A bits
     * @return
     */
    public abstract int getAlphaSize();
}
