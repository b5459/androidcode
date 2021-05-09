package com.example.mqtt_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyLocationStyleCreator;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
/*
第二节：
控件（常用）：
按钮（图片按钮，普通按钮）：颜色，背景
文本框
编辑框
图片框
选择开关switch:::
控件（扩展）
浏览器框
地图
拖动条
进度条
复选框
单选框
wrap_content  自适应
match_parent  充满父控件
android:id="@+id/bt_1"  用来和java文件通讯或者绑定事件的
LinearLayout 线性布局，好用的
android:orientation="vertical":设置布局线性进行方向vertical为垂直 horizontal为水平
android:gravity="center":设置控件开始的布局位置，centre中心 bottom 底部
android:layout_::针对自己的部件的控制
android:layout_margin="10dp"    边距
android:layout_weight="1"     中心为1来均分
<RelativeLayout  相对布局  android:layout_alignParentBottom="true"  贴紧父元素的下边缘
第三节：
控件的ID，是JAVA文件与XML文件的通讯的价值，类似于控件号码牌，
单击事件有很多种实现方法

安卓开发 要多调试，多刷程序，因为不知什么时候会崩
java里面的操作，大部分类似于单片机的函数

按钮单击后，用来发送命令，控制硬件，例如opendoor
文本框用来更新数据，接收硬件上报的传感器值，例如 温度 25.6

按需求寻找自己需要组合的控件，直接面向项目和实战。
举一反三。第三节结束

第四节
先复制
Mqtt_init()
Mqtt_connect()
startReconnect()
爆红忽略
最后复制
Handle()
###########
MQTT的知识  保证每个人都能连我的服务器   每个人的ID（MQTT要求唯一）都这设置为自己的QQ号
先刷手机在讲原理
需要网络权限和授权！（动态授权后面再讲，android6.0
连接成功
开始订阅topic
拿到数据  可以设置号自己想要的可视化界面，用好的UI来展示数据和布局

下发控制：
发布消息到指定topic
拷贝发布函数，阿正改善的函数，在该页的最底

测试app发消息成功，硬件接收消息成功
硬件得到app发送的消息， 判断是不是open——led，是，然后点灯
app得到硬件发送的消息， 判断是哪个传感器数据， 是，然后展示 温度56.3

依据这个例子，进行举一反三

第五节
app远程控制硬件点灯
发布消息给硬件订阅topic
开灯：{"set_led":1}
关灯：{"set_led":0}

硬件上报温度给手机APP
发布消息给APP订阅的TOPIC
{"temperature":23}

剩下的举一反三。你可将数据装在更好看的UI上比如仪表盘或者其他
同理，将收集到的地图数据展示出来，并按在合适的地图UI上，结合高德的sdk或者其他，将坐标和路线准确展示出来即可

下节课讲json



*/


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements AMap.OnMyLocationChangeListener,GeocodeSearch.OnGeocodeSearchListener {
    //第三day
    private Button btn_1; //类似于单片机开发的里面的初始化  参数初始化
    private ImageView image_1;
    private TextView text_test;
    //第四day
    private String host = "tcp://iot.qaeb.cn:31883";
    private String userName = "emqx";
    private String passWord = "hugeos";
    private String mqtt_id = "545988054study";//必须唯一
    private String mqtt_sub_topic = "545988054"; //必须唯一为了保证你不受到别人的消息  哈哈
    private String mqtt_pub_topic = "545988054_ESP32"; //必须唯一为了保证你不受到别人的消息  哈哈  自己QQ好后面加 _PC
    private ScheduledExecutorService scheduler;
    private MqttClient client;//定义mqttclient变量，名字为client
    private MqttConnectOptions options;
    private Handler handler;
    //第五day
    private int led_flag = 1;

    private MapView mapView;
    private AMap aMap;

    private boolean isFirLocate = true;
    private Double mylongitude = 0.0;
    private Double mylatitude = 0.0;
    private LatLng ESP_location;
    private Location local;
    private GeocodeSearch geocoderSearch;

    private Button btn_location_name1;
    private TextView text_location_name1;
    private Button btn_location_name2;
    private TextView text_location_name2;
    private int location_flag = 0;
    private RegeocodeAddress address_location1 = null;
    private RegeocodeAddress address_location2 = null;
    private ImageView image_2;
    private ImageView image_3;
    private int ESP_flag = 0;

    public static double[] coords = new double[100];
    public static int k=0;



    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //这里是界面打开后最先运行的地方
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//对应的界面UI

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 创建地图
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        btn_location_name1 = (Button)findViewById(R.id.btn_location_name1);
        text_location_name1 = (TextView)findViewById(R.id.text_location_name1);
        btn_location_name2 = (Button)findViewById(R.id.btn_location_name2);
        text_location_name2 = (TextView)findViewById(R.id.text_location_name2);
        image_1 = (ImageView) findViewById(R.id.image_1);
        image_2 = (ImageView) findViewById(R.id.image_2);
        image_3 = (ImageView) findViewById(R.id.image_3);

        init();
        btn_location_name1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location1(address_location1);
            }
        });

        btn_location_name2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location2(address_location2);
            }
        });
        image_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ESP_flag == 1){
                    Toast.makeText(MainActivity.this, "已定位", Toast.LENGTH_SHORT).show();
                    navigateTO(ESP_location);
                    ESP_flag = 0;
                    coords[k++] = ESP_location.longitude;
                    coords[k++] = ESP_location.latitude;
                }else Toast.makeText(MainActivity.this, "ESP未发送定位数据", Toast.LENGTH_SHORT).show();
            }
        });
        image_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(k > 2) {
                    Toast.makeText(MainActivity.this, "已绘制", Toast.LENGTH_SHORT).show();
                    addPolylineInPlayGround();
                    //k = 0;//先不清零。清零的话绘制完一次后需重新发两个点
                }else Toast.makeText(MainActivity.this, "未定位或定位点少于2", Toast.LENGTH_SHORT).show();
            }
        });



        //一般先用来进行界面初始化，控件初始化，初始化一些参数和变量
        //类似与单片机的main函数
        //以上为第一节
        btn_1 = findViewById(R.id.btn_1); //寻找xml里面的真正的ID与自己定义的id绑定
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这里就是单击执行后的地方
                //单片机的调试printf("hellp")
                //System.out.println("hello");
                Log.d("TAG","hello");
                //更直观的方法 用弹窗 toast
                //在当前activity显示内容为“hello”的短时间弹窗   length_short为短时间
                Toast.makeText(MainActivity.this, "hello", Toast.LENGTH_SHORT).show();
            }
        });
        //到这里基本学会安卓看法
        //按钮单击事件会了，到图片单击事件。
        image_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "我是第一个图片", Toast.LENGTH_SHORT).show();
                if(led_flag == 0)
                {
                    publishmessageplus(mqtt_pub_topic,"{\"set_led\":1}");
                    led_flag =1;
                }
                else {
                    publishmessageplus(mqtt_pub_topic,"{\"set_led\":0}");
                    led_flag =0;
                }
                aMap.clear();//清除轨迹和marker
                //client.publish(mqtt_pub_topic,"open led");
            }
        });
        //两个控件联动，按钮单击更改textview的值
        text_test = findViewById(R.id.text_test);
        //********************************************
        Mqtt_init();
        startReconnect();
        handler = new Handler() {
            public void handleMessage(Message  msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传

                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());

//                        String msg_val = msg.obj.toString().substring(msg.obj.toString().indexOf("---")+3);
//                        String text_val = "数据信息：" + msg_val;
//                        text_test.setText(text_val);
                        //Toast.makeText(MainActivity.this,text_val,Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this,text_val,Toast.LENGTH_SHORT).show();
                        StringBuilder currentPoisition = new StringBuilder();
                        currentPoisition.append("经度：").append( msg.obj.toString().substring(msg.obj.toString().indexOf("\"Longitude\":")+12,msg.obj.toString().indexOf(",\"E\""))).append("E\n");
                        currentPoisition.append("纬度：").append( msg.obj.toString().substring(msg.obj.toString().indexOf("\"Latitude\":")+11,msg.obj.toString().indexOf(",\"N\""))).append("N\n");
                        currentPoisition.append("高度：").append( msg.obj.toString().substring(msg.obj.toString().indexOf("\"Altitude\":")+11,msg.obj.toString().indexOf(",\"M\""))).append("M\n");
                        text_test.setText(currentPoisition);
                        //实现GPS数据经纬度的提取和转换
                        mylongitude = Double.valueOf(msg.obj.toString().substring(msg.obj.toString().indexOf("\"Longitude\":")+12,msg.obj.toString().indexOf(",\"E\"")));
                        mylatitude = Double.valueOf(msg.obj.toString().substring(msg.obj.toString().indexOf("\"Latitude\":")+11,msg.obj.toString().indexOf(",\"N\"")));
                        LatLng sourceLatLng = new LatLng(mylatitude, mylongitude);
                        ESP_location = convert(sourceLatLng, CoordinateConverter.CoordType.GPS);
                        ESP_flag = 1;

                        //navigateTO(ESP_location);
                        break;
                    case 30:  //连接失败
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 31:   //连接成功
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        try {
                            client.subscribe(mqtt_sub_topic,1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        if (mapView != null) {
            mapView.onDestroy();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    //初始化定位和获取地址到变量
    private void init() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
        myLocationStyle.interval(2000);//设置连续定位模式下的定位间隔，在连续定位下生效
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点styte

        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需
        aMap.setMyLocationEnabled(true);//设置true表示启动显示定位蓝点，默认false表示隐藏定位蓝点不定位
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17f));//缩放17
        aMap.setOnMyLocationChangeListener(this);
    }

    @Override
    public void onMyLocationChange(Location location) {
        local = location;
//        DPoint ll =new DPoint();
//        ll.setLongitude(location.getLongitude());
//        ll.setLatitude(location.getLatitude());
        //Log.e("TAG", "MyLocation=[" + location.getLongitude() + ", " + location.getLatitude() + "]");

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        //拿到数据使用逆地理编码解析地址（坐标转地址）
        LatLonPoint latLonPoint = new LatLonPoint(local.getLatitude(), local.getLongitude());
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
        location_flag = 2;
    }
    private void location1(RegeocodeAddress address){
        if(address_location1 != null) {
            StringBuilder currentPoisition2 = new StringBuilder();
            currentPoisition2.append(address.getFormatAddress()).append("附近");
            text_location_name1.setText(currentPoisition2);
        }
    }

    private void location2(RegeocodeAddress address){
        if(address_location2 != null) {
            StringBuilder currentPoisition2 = new StringBuilder();
            currentPoisition2.append(address.getFormatAddress()).append("附近");
            text_location_name2.setText(currentPoisition2);
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int code) {
        Log.e("TAG", "code=" + code);
        RegeocodeAddress address = result.getRegeocodeAddress();
        if (address == null) {
            Log.e("TAG", "结果为空");
        }
//        else {
//            Log.e("TAG", "地址：" + address.getProvince() + address.getCity() + address.getDistrict());
//        }
        if(location_flag == 1) {
            address_location1 = address;
            location_flag = 0;
        }
        if (location_flag ==2){
            address_location2 = address;
            location_flag = 0;
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    //转换
    private LatLng convert(LatLng sourceLatLng, CoordinateConverter.CoordType coord ) {
        CoordinateConverter converter  = new CoordinateConverter(this);
        // CoordType.GPS 待转换坐标类型
        converter.from(coord);
        // sourceLatLng待转换坐标点
        converter.coord(sourceLatLng);
        // 执行转换操作
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    private void changeCamera(CameraUpdate update) {
        aMap.moveCamera(update);
    }


    private void navigateTO(LatLng ll){
        changeCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        ll, 18, 30, 0)));
        //aMap.clear();//清除轨迹和marker
        aMap.addMarker(new MarkerOptions().position(ll)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptor.CONTENTS_FILE_DESCRIPTOR)));//BitmapDescriptorFactory.HUE_BLUE
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        //拿到数据使用逆地理编码解析地址（坐标转地址）
        LatLonPoint latLonPoint = new LatLonPoint(ESP_location.latitude, ESP_location.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
        location_flag = 1;
    }

    private void addPolylineInPlayGround() {
        List<LatLng> list = readLatLngs();
        //List<LatLng> list = showListLat1();
        //List<Integer> colorList = new ArrayList<Integer>();

        aMap.addPolyline(new PolylineOptions() //setCustomTextureList(bitmapDescriptors)
                .addAll(list)
                //.add(new LatLng(23.1073733, 113.475585),new LatLng(23.1073733,113.475685),new LatLng(23.1073733, 113.476085), new LatLng(23.1073733, 113.476585))
                .width(10)
                .color(Color.BLACK));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(list.get(0));
        builder.include(list.get(list.size() - 2));
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

    }

    /**
     * 读取坐标点
     * @return
     */
    private List<LatLng> readLatLngs() {
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < k; i += 2) {
            points.add(new LatLng(coords[i + 1], coords[i]));//latlng （纬，经）
        }
        return points;
    }

    public double[] coordsll = GetPoint();
    public int len = GetPointLen();
    public static double[] GetPoint(){
        return coords;
    }

    public static int GetPointLen(){
        return k;
    }

    private List<LatLng> showListLat1(){
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < coords1.length; i += 2) {
            points.add(new LatLng(coords1[i+1], coords1[i]));
        }
        return points;
    }
    private double[] coords1 = {
            113.475585,23.107373,
            113.475685,23.107373,
            113.476085,23.107373,
            113.476585,23.107373
    };

    //********************************************************************
    //第四天
    //mqtt初始化
    private void Mqtt_init()
    {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host,mqtt_id,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(70);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(100);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    //startReconnect();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }
                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    msg.what = 3;   //收到消息标志位
                    msg.obj = topicName + "---" + message.toString();
                    handler.sendMessage(msg);    // hander 回传
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!(client.isConnected()) )  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }
    private void publishmessageplus(String topic,String message2)
    {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(topic,message);
        } catch (MqttException e) {

            e.printStackTrace();
        }
    }



}
