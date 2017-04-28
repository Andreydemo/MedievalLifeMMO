package org.demosoft.medieval.life.gameserver;

/**
 * Created by Andrii on 4/10/2017.
 */
public class Config {
    public static final String MAXIMUM_ONLINE_USERS = "25";
    public static String SERVER_VERSION;

    public static String DATAPACK_VERSION;
    public static int THREAD_P_EFFECTS;
    public static int THREAD_P_GENERAL;
    public static int IO_PACKET_THREAD_CORE_SIZE;
    public static int GENERAL_PACKET_THREAD_CORE_SIZE;
    public static int GENERAL_THREAD_CORE_SIZE;
    public static int AI_MAX_THREAD;
    public static boolean DEBUG;
    public static int MIN_PROTOCOL_REVISION;
    public static int MAX_PROTOCOL_REVISION;
    public static String GAMESERVER_HOSTNAME = "localhost";
    public static int PORT_GAME = 5000;
}
