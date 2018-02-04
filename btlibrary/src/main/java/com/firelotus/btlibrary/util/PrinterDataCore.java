package com.firelotus.btlibrary.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Iterator;
import java.util.List;

public class PrinterDataCore {
    public int BitmapWidth = 0;
    public int PrintDataHeight = 0;
    public byte HalftoneMode = 1;
    public byte ScaleMode = 0;
    public byte CompressMode = 0;
    private int B = 0;
    private int C = 0;

    public PrinterDataCore() {
    }

    public byte[] PrintDataFormat(Bitmap var1) {
        try {
            byte[] var3;
            if (this.HalftoneMode > 0) {
                var3 = this.a(var1);
            } else {
                var3 = this.b(var1);
            }

            return var3;
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    private byte[] a(Bitmap var1) {
        int var2 = var1.getWidth();
        int var3 = var1.getHeight();
        boolean var4 = false;
        boolean var5 = false;
        boolean var6 = false;
        boolean var7 = false;
        int var8 = 0;
        boolean var9 = false;
        int var10 = var2 + 7 >> 3;
        Object var11 = null;
        Object var12 = null;

        try {
            this.PrintDataHeight = var3;
            this.BitmapWidth = var10;
            int var16 = var2 * var3;
            int var18 = var10 * var3;
            int[] var22 = new int[var16];
            var1.getPixels(var22, 0, var2, 0, 0, var2, var3);

            int var17;
            for (var17 = 0; var17 < var16; ++var17) {
                int var14 = var22[var17];
                var22[var8++] = 255 & (byte) ((int) ((double) Color.red(var14) * 0.29891D + (double) Color.green(var14) * 0.58661D + (double) Color.blue(var14) * 0.11448D));
                if (var17 == 112000) {
                    System.out.println("");
                }

                if (var17 == 223999) {
                    System.out.println("");
                }

                if (var17 == 168000) {
                    System.out.println("");
                }
            }

            for (var17 = 0; var17 < var3; ++var17) {
                var8 = var17 * var2;

                for (var16 = 0; var16 < var2; ++var16) {
                    float var15;
                    if (var22[var8] > 128) {
                        var15 = (float) (var22[var8] - 255);
                        var22[var8] = 255;
                    } else {
                        var15 = (float) (var22[var8] - 0);
                        var22[var8] = 0;
                    }

                    if (var16 < var2 - 1) {
                        var22[var8 + 1] += (int) (0.4375D * (double) var15);
                    }

                    if (var17 < var3 - 1) {
                        if (var16 > 1) {
                            var22[var8 + var2 - 1] += (int) (0.1875D * (double) var15);
                        }

                        var22[var8 + var2] += (int) (0.3125D * (double) var15);
                        if (var16 < var2 - 1) {
                            var22[var8 + var2 + 1] += (int) (0.0625D * (double) var15);
                        }
                    }

                    ++var8;
                }

                if (var17 == 140) {
                    System.out.println("");
                }

                if (var17 == 279) {
                    System.out.println("");
                }

                if (var17 == 210) {
                    System.out.println("");
                }
            }

            byte[] var21 = new byte[var18];

            label98:
            for (var17 = 0; var17 < var3; ++var17) {
                var8 = var17 * var2;
                int var20 = var17 * var10;
                var18 = 0;
                var16 = 0;

                while (true) {
                    int var19;
                    do {
                        if (var16 >= var2) {
                            continue label98;
                        }

                        var19 = var16 % 8;
                        if (var22[var8++] <= 128) {
                            var18 |= 128 >> var19;
                        }

                        ++var16;
                    } while (var19 != 7 && var16 != var2);

                    var21[var20++] = (byte) var18;
                    var18 = 0;
                    if (var20 == 14000) {
                        System.out.println("");
                    }

                    if (var20 == 28000) {
                        System.out.println("");
                    }

                    if (var20 == 21000) {
                        System.out.println("");
                    }
                }
            }

            return var21;
        } catch (Exception var13) {
            var13.printStackTrace();
            return null;
        }
    }

    private byte[] b(Bitmap var1) {
        Object var5 = null;
        boolean var7 = false;
        boolean var8 = false;
        boolean var6 = false;
        int var9 = 0;
        int var10 = 0;
        boolean var11 = false;

        try {
            int var2 = var1.getWidth();
            int var3 = var1.getHeight();
            this.PrintDataHeight = var3;
            int var4 = var2 % 8 == 0 ? var2 : (var2 / 8 + 1) * 8;
            this.BitmapWidth = var4 / 8;
            byte[] var15 = new byte[var4 = var3 * this.BitmapWidth];

            for (int var12 = 0; var12 < var4; ++var12) {
                var15[var12] = 0;
            }

            while (var9 < var3) {
                int[] var20 = new int[var2];
                var1.getPixels(var20, 0, var2, 0, var9, var2, 1);
                int var19 = 0;

                for (int var13 = 0; var13 < var2; ++var13) {
                    ++var19;
                    int var16 = var20[var13];
                    if (var19 > 8) {
                        var19 = 1;
                        ++var10;
                    }

                    if (var16 != -1) {
                        var4 = 1 << 8 - var19;
                        int var17 = Color.red(var16);
                        int var18 = Color.green(var16);
                        var16 = Color.blue(var16);
                        if ((var17 + var18 + var16) / 3 < 128) {
                            var15[var10] = (byte) (var15[var10] | var4);
                        }
                    }
                }

                var10 = this.BitmapWidth * (var9 + 1);
                ++var9;
            }

            return var15;
        } catch (Exception var14) {
            var14.printStackTrace();
            return null;
        }
    }

    public byte[] sysCopy(List<byte[]> var1) {
        int var2 = 0;

        byte[] var3;
        for (Iterator var4 = var1.iterator(); var4.hasNext(); var2 += var3.length) {
            var3 = (byte[]) var4.next();
        }

        var3 = new byte[var2];
        int var7 = 0;

        byte[] var5;
        for (Iterator var6 = var1.iterator(); var6.hasNext(); var7 += var5.length) {
            System.arraycopy(var5 = (byte[]) var6.next(), 0, var3, var7, var5.length);
        }

        return var3;
    }
}
