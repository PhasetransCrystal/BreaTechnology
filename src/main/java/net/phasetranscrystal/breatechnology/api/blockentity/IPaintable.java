package net.phasetranscrystal.breatechnology.api.blockentity;

/**
 * @implNote 在可绘制的方块实体上实现
 */
public interface IPaintable {

    /**
     * 获取绘制颜色。
     * 这不是该方块的真实颜色。
     * 
     * @return -1 表示未绘制
     */
    int getPaintingColor();

    void setPaintingColor(int color);

    /**
     * 默认颜色。
     */
    int getDefaultPaintingColor();

    /**
     * 判断方块是否被绘制。
     */
    default boolean isPainted() {
        return getPaintingColor() != -1 && getPaintingColor() != getDefaultPaintingColor();
    }

    /**
     * 获取该方块的真实颜色。
     */
    default int getRealColor() {
        return isPainted() ? getPaintingColor() : getDefaultPaintingColor();
    }
}
