package com.firelotus.btlibrary;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.firelotus.btlibrary.constant.ConstantDefine;
import com.firelotus.btlibrary.util.PrinterHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class BtCmdService extends IntentService {

    private static final String TAG = BtCmdService.class.getSimpleName();
    private BluetoothSdkManager manager;

    public BtCmdService() {
        super("BtCmdService");
    }

    public BtCmdService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = BluetoothSdkManager.INSTANCE;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (action.equals(ConstantDefine.ACTION_PRINT_TEST)) {
            printTest();
        } else if (action.equals(ConstantDefine.ACTION_PRINT_ST_ONE)) {
            printSTOne();
        } else if (action.equals(ConstantDefine.ACTION_PRINT_ST_TWO)) {
            printSTTwo();
        } else if (action.equals(ConstantDefine.ACTION_GET_STATUS)) {
            getStatus();
        } else if (action.equals(ConstantDefine.ACTION_GET_PRINT_NAME)) {
            getPrintName();
        } else if (action.equals(ConstantDefine.ACTION_GET_PRINT_VERSION)) {
            getPrintVersion();
        } else if (action.equals(ConstantDefine.ACTION_GET_PRINT_ID)) {
            getPrintID();
        } else if (action.equals(ConstantDefine.ACTION_PRINT)) {
            print(intent.getByteArrayExtra(ConstantDefine.ACTION_PRINT_EXTRA));
        } else if (action.equals(ConstantDefine.ACTION_RESET)) {
            reSet();
        }
    }

    private void reSet() {
        //复位
        //manager.print(new byte[]{0x1b, 0x40});

        //清除禁止打印状态
        manager.print(new byte[]{0x1b, 0x41});

        //设备状态复位
        //manager.print(new byte[]{0x10,0x06,0x07,0x08,0x08});

        //feed line
        //manager.print(new byte[]{10});
    }

    private void getStatus() {
        try {
            int getstatus = PrinterHelper.getStatus();
            switch (getstatus) {
                case 0:
                    Log.d(TAG, "打印机准备就绪");
                    break;
                case 1:
                    Log.d(TAG, "打印机打印中");
                    break;
                case 2:
                    Log.d(TAG, "打印机缺纸");
                    break;
                case 6:
                    Log.d(TAG, "打印机开盖");
                    break;
                default:
                    Log.d(TAG, "出错");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintName() {
        try {
            Log.d(TAG, PrinterHelper.getPrintName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintVersion() {
        try {
            Log.d(TAG, PrinterHelper.getPrintVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPrintID() {
        try {
            Log.d(TAG, PrinterHelper.getPrintID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printTest() {
        /*ArrayList<byte[]> bytes = new ArrayList<byte[]>();
        bytes.add("! 0 200 200 500 1\r\n".getBytes());
        bytes.add("TEXT 4 0 200 100 TEXT\r\n".getBytes());
        bytes.add("TEXT90 4 0 200 100 T90\r\n".getBytes());
        bytes.add("TEXT180 4 0 200 100 T180\r\n".getBytes());
        bytes.add("TEXT270 4 0 200 100 T270\r\n".getBytes());
        bytes.add("FORM\r\n".getBytes());
        bytes.add("PRINT\r\n".getBytes());
        manager.print(bytes);*/

        /*manager.print("! 0 200 200 500 1\r\n".getBytes());
        manager.print("TEXT 4 0 200 100 TEXT\r\n".getBytes());
        manager.print("TEXT90 4 0 200 100 T90\r\n".getBytes());
        manager.print("TEXT180 4 0 200 100 T180\r\n".getBytes());
        manager.print("TEXT270 4 0 200 100 T270\r\n".getBytes());
        manager.print("FORM\r\n".getBytes());
        manager.print("PRINT\r\n".getBytes());*/

        try {
            PrinterHelper.printAreaSize("0", "200", "200", "500", "1");
            PrinterHelper.Text(PrinterHelper.TEXT, "4", "0", "200", "100", "TEXT");
            PrinterHelper.Text(PrinterHelper.TEXT90, "4", "0", "200", "100", "TEXT90");
            PrinterHelper.Text(PrinterHelper.TEXT180, "4", "0", "200", "100", "TEXT180");
            PrinterHelper.Text(PrinterHelper.TEXT270, "4", "0", "200", "100", "TEXT270");
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*ArrayList<byte[]> bytes = new ArrayList<byte[]>();
        bytes.add("! 0 200 200 500 1\r\n".getBytes());
        bytes.add("IN-INCHES\r\n".getBytes());
        bytes.add("T 4 0 0 0 1 cm = 0.3937\r\n".getBytes());
        //bytes.add("BOX 0 0 200 200 1\r\n".getBytes());
        //bytes.add("BOX 200 200 400 400 1\r\n".getBytes());
        bytes.add("IN-DOTS\r\n".getBytes());
        bytes.add("T 4 0 0 48 1 mm = 8 dots\r\n".getBytes());
        bytes.add("B 128 1 1 48 16 112 UNITS\r\n".getBytes());
        bytes.add("T 4 0 48 160 UNITS\r\n".getBytes());
        bytes.add("FORM\r\n".getBytes());
        bytes.add("PRINT\r\n".getBytes());
        manager.print(bytes);*/

        /*try{
            PrinterHelper.printAreaSize("0","200","200","500","1");
            PrinterHelper.ML("47");
            PrinterHelper.Text(PrinterHelper.TEXT,"4","0","10","20","上海市宝山区共和新路\r\n上海市宝山区共和新路\r\n上海市宝山区共和新路\r\n上海市宝山区共和新路\r\n");
            PrinterHelper.ENDML();
            PrinterHelper.Print();
        }catch (Exception e){

        }*/
    }

    private void printSTOne() {
        try {
            HashMap<String, String> pum = new HashMap<String, String>();
            pum.put("[barcode]", "363604310467");
            pum.put("[distributing]", "上海 上海市 长宁区");
            pum.put("[receiver_name]", "申大通");
            pum.put("[receiver_phone]", "13826514987");
            pum.put("[receiver_address1]", "上海市宝山区共和新路4719弄共");
            pum.put("[receiver_address2]", "和小区12号306室");//收件人地址第一行
            pum.put("[sender_name]", "快小宝");//收件人第二行（若是没有，赋值""）
            pum.put("[sender_phone]", "13826514987");//收件人第三行（若是没有，赋值""）
            pum.put("[sender_address1]", "上海市长宁区北曜路1178号（鑫达商务楼）");
            pum.put("[sender_address2]", "1号楼305室");
            Set<String> keySet = pum.keySet();
            Iterator<String> iterator = keySet.iterator();
            InputStream afis = this.getResources().getAssets().open("STO_CPCL.txt");//打印模版放在assets文件夹里
            String path = new String(InputStreamToByte(afis), "utf-8");//打印模版以utf-8无bom格式保存
            while (iterator.hasNext()) {
                String string = iterator.next();
                path = path.replace(string, pum.get(string));
            }
            PrinterHelper.printText(path);
            InputStream inbmp = this.getResources().getAssets().open("logo_sto_print1.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inbmp);
            InputStream inbmp2 = this.getResources().getAssets().open("logo_sto_print2.png");
            Bitmap bitmap2 = BitmapFactory.decodeStream(inbmp2);
            PrinterHelper.Expanded("10", "20", bitmap, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "712", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "1016", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {

        }
    }

    private void printSTTwo() {
        try {
            HashMap<String, String> pum = new HashMap<String, String>();
            pum.put("[barcode]", "363604310467");
            pum.put("[distributing]", "上海 上海市 长宁区");
            pum.put("[receiver_name]", "申大通");
            pum.put("[receiver_phone]", "13826514987");
            pum.put("[receiver_address1]", "上海市宝山区共和新路4719弄共");
            pum.put("[receiver_address2]", "和小区12号306室");//收件人地址第一行
            pum.put("[sender_name]", "快小宝");//收件人第二行（若是没有，赋值""）
            pum.put("[sender_phone]", "13826514987");//收件人第三行（若是没有，赋值""）
            pum.put("[sender_address1]", "上海市长宁区北曜路1178号（鑫达商务楼）");
            pum.put("[sender_address2]", "1号楼305室");
            InputStream afis = this.getResources().getAssets().open("STO_CPCL.txt");//打印模版放在assets文件夹里
            PrinterHelper.printSingleInterface(afis, pum);

            InputStream inbmp = this.getResources().getAssets().open("logo_sto_print1.png");
            Bitmap bitmap = BitmapFactory.decodeStream(inbmp);
            InputStream inbmp2 = this.getResources().getAssets().open("logo_sto_print2.png");
            Bitmap bitmap2 = BitmapFactory.decodeStream(inbmp2);
            PrinterHelper.Expanded("10", "20", bitmap, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "712", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Expanded("10", "1016", bitmap2, (byte) 0);//向打印机发送LOGO
            PrinterHelper.Form();
            PrinterHelper.Print();
        } catch (Exception e) {

        }
    }

    private void print(byte[] bytes) {
        if (null == bytes || bytes.length <= 0) {
            return;
        }
        manager.print(bytes);
    }

    private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }


}
