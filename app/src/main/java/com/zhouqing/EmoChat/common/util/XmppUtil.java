package com.zhouqing.EmoChat.common.util;

import android.util.Log;

import com.zhouqing.EmoChat.common.constant.Global;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class XmppUtil {

    private static final String TAG = "XmppUtil";
    private static XMPPConnection connection;

    public static XMPPConnection getConnection() {
        return connection;
    }

    /**
     * 连接服务器
     *
     * @return
     */
    public static boolean conServer() {

        ConnectionConfiguration config = new ConnectionConfiguration(
                Global.HOST, Global.PORT);
        //明文传输
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//        /** 是否启用安全验证 */
//        config.setSASLAuthenticationEnabled(false);
        /** 是否启用调试 */
        config.setDebuggerEnabled(true);
        /** 创建connection链接 */
        try {
            connection = new XMPPConnection(config);
            /** 建立连接 */
            connection.connect();
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 注册
     *
     * @param account  注册帐号
     * @param password 注册密码
     * @return 1、注册成功 0、服务器没有返回结果2、这个账号已经存在3、注册失败
     */
    public static int regist(String account, String password) {
        Registration reg = new Registration();
        reg.setType(IQ.Type.SET);
        reg.setTo(connection.getServiceName());
        reg.setUsername(account);// 注意这里createAccount注册时，参数是username，不是jid，是“@”前面的部分。
        reg.setPassword(password);
        reg.addAttribute("android", "geolo_createUser_android");// 这边addAttribute不能为空，否则出错。所以做个标志是android手机创建的吧！！！！！


        PacketFilter filter = new AndFilter(new PacketIDFilter(
                reg.getPacketID()), new PacketTypeFilter(IQ.class));
        PacketCollector collector = connection.createPacketCollector(filter);
        connection.sendPacket(reg);
        IQ result = (IQ) collector.nextResult(SmackConfiguration
                .getPacketReplyTimeout());
        // Stop queuing results
        collector.cancel();// 停止请求results（是否成功的结果）


        if (result == null) {
            Log.e("RegisterActivity", "No response from server.");
            return 0;
        } else if (result.getType() == IQ.Type.RESULT) {
            return 1;
        } else { // if (result.getType() == IQ.Type.ERROR)
            if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
                Log.e("RegisterActivity", "IQ.Type.ERROR: "
                        + result.getError().toString());
                return 2;
            } else {
                Log.e("RegisterActivity", "IQ.Type.ERROR: "
                        + result.getError().toString());
                return 3;
            }
        }
    }

    //登陆
    public static boolean login(String a, String p) {
        try {
            connection.login(a, p);

            Log.d(TAG, "zq:"+getUserState("zq"));
            Log.d(TAG, "lll:"+getUserState("lll"));
            Log.d(TAG, "cly:"+getUserState("cly"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改密码
     *
     * @param connection
     * @return
     */
    public static boolean changePassword(XMPPConnection connection, String pwd) {
        try {
            connection.getAccountManager().changePassword(pwd);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 注销
     * 删除当前用户
     *
     * @param connection
     * @return
     */
    public static boolean deleteAccount(XMPPConnection connection) {
        try {
            connection.getAccountManager().deleteAccount();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前用户头像
     */
    public static int getCurrentUserAvatar(){
        ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new org.jivesoftware.smackx.provider.VCardProvider());
        VCard vCard = new VCard();
        try {
            vCard.load(connection);
            if(vCard.getField("avatarId") != null){
                return Integer.parseInt(vCard.getField("avatarId"));
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取其他用户头像
     */
    public static String getOtherUserAvatar(String jid){
        ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new org.jivesoftware.smackx.provider.VCardProvider());
        VCard vCard = new VCard();
        try {
            vCard.load(connection,jid);
            if(vCard.getField("avatarId") != null){
                return vCard.getField("avatarId");
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除好友
     */
    public static void deleteFriend(String user){
        Roster roster = connection.getRoster();
        RosterEntry entry = roster.getEntry(user);
        try {
            roster.removeEntry(entry);
        }catch (XMPPException e){
            e.printStackTrace();
        }
    }

    /**
     * 判断用户状态 0-不存在 1-在线 2-离线
     */
    public static int getUserState(String user){
        String url = "http://"+Global.HOST+":9090/plugins/presence/status?" +
                "jid="+ user +"@"+ Global.HOST +"&type=xml";
        int shOnLineState = 0; // 不存在
        try {
            URL oUrl = new URL(url);
            URLConnection oConn = oUrl.openConnection();
            if (oConn != null) {
                BufferedReader oIn = new BufferedReader(new InputStreamReader(
                        oConn.getInputStream()));
                if (null != oIn) {
                    String strFlag = oIn.readLine();
                    oIn.close();
                    System.out.println("strFlag"+strFlag);
                    if (strFlag.indexOf("type=\"unavailable\"") >= 0) {
                        shOnLineState = 2;
                    }
                    if (strFlag.indexOf("type=\"error\"") >= 0) {
                        shOnLineState = 0;
                    } else if (strFlag.indexOf("priority") >= 0
                            || strFlag.indexOf("id=\"") >= 0) {
                        shOnLineState = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shOnLineState;
    }
}
