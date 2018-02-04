package com.firelotus.btlibrary.constant;

public class ConstantDefine {

    //定义当前的连接状态
    public static final int CONNECT_STATE_NONE = 0;         //什么都没有连接
    public static final int CONNECT_STATE_LISTENER = 1;     //侦听连接
    public static final int CONNECT_STATE_CONNECTING = 2;   //正在连接
    public static final int CONNECT_STATE_CONNECTED = 3;    //已经连接


    //Handler的消息类型
    public static final int MESSAGE_STATE_READ = 1;
    public static final int MESSAGE_STATE_WRITE = 2;
    public static final int MESSAGE_STATE_CHANGE = 3;
    public static final int MESSAGE_DEVICE_INFO = 4;

    //需要的UUID
    public static final String STRING_DEVICE_PRINTER = "00001101-0000-1000-8000-00805F9B34FB";

    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_ADDRESS = "device_address";
    //蓝牙打印自定义命令
    public static final String ACTION_PRINT_TEST = "action_print_test";
    public static final String ACTION_PRINT = "action_print";

    public static final String ACTION_PRINT_EXTRA = "action_print_extra";

    public static final String ACTION_PRINT_ST_ONE = "print_st_one";
    public static final String ACTION_PRINT_ST_TWO = "print_st_two";

    public static final String ACTION_GET_STATUS = "action_get_status";
    public static final String ACTION_GET_PRINT_NAME = "action_get_print_name";
    public static final String ACTION_GET_PRINT_VERSION = "action_get_print_version";
    public static final String ACTION_GET_PRINT_ID = "action_get_print_id";
    public static final String ACTION_RESET = "action_reset";
}
