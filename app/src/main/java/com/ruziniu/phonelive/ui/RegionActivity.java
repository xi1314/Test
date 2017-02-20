package com.ruziniu.phonelive.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.cache.City;
import com.ruziniu.phonelive.utils.PingYinUtil;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.MyLetterListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.Call;

/**
 * Created by admin on 2016/9/27.
 */
public class RegionActivity extends ToolBarBaseActivity implements AbsListView.OnScrollListener {

    private BaseAdapter adapter;
    private ResultListAdapter resultListAdapter;
    private ListView personList;
    private ListView  resultList;
    private TextView overlay; // 对话框首字母textview
    private MyLetterListView letterListView; // A-Z listview
    private HashMap<String, Integer> alphaIndexer;// 存放存在的汉语拼音首字母和与之对应的列表位置
    private String[] sections;// 存放存在的汉语拼音首字母
    private Handler handler;
    private OverlayThread overlayThread; // 显示首字母对话框
    private ArrayList<City> allCity_lists; // 所有城市列表
    private ArrayList city_lists = new ArrayList();// 城市列表
    private ArrayList<City> city_hot;
    private ArrayList<City> city_result;
    private ArrayList<String> city_history;
    private EditText sh;
    private TextView tv_noresult;
    private String currentCity ; // 用于保存定位到的城市
    private int locateProcess = 1; // 记录当前定位的状态 正在定位-定位成功-定位失败
    private boolean isNeedFresh;
    private ImageView mBack;
    private TextView mSearch;

    //private DatabaseHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region);
        personList = (ListView) findViewById(R.id.list_view);
        ImageView mBack = (ImageView) findViewById(R.id.tv_back);
        mSearch = (TextView) findViewById(R.id.tv_regionSearch);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backDate();
                finish();
            }
        });
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((sh.getText().toString()).equals("")){
                    Toast.makeText(getApplicationContext(),
                            "请输入有效地区", Toast.LENGTH_SHORT)
                            .show();
                }else{
                    searchRegion();
                }
            }
        });
        allCity_lists = new ArrayList<City>();
        city_hot = new ArrayList<City>();
        city_result = new ArrayList<City>();
        city_history = new ArrayList<String>();
        resultList = (ListView) findViewById(R.id.search_result);
        sh = (EditText) findViewById(R.id.sh);
        tv_noresult = (TextView) findViewById(R.id.tv_noresult);
        letterListView = (MyLetterListView) findViewById(R.id.MyLetterListView01);
        letterListView
                .setOnTouchingLetterChangedListener(new LetterListViewListener());
        alphaIndexer = new HashMap<String, Integer>();
        handler = new Handler();
        overlayThread = new OverlayThread();
        isNeedFresh = true;
        personList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position >= 4) {
                    currentCity = allCity_lists.get(position).getName();
                    backDate();
                    finish();
                }
            }
        });
        locateProcess = 1;
        personList.setAdapter(adapter);
        personList.setOnScrollListener(this);

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                currentCity = city_lists.get(position).toString();
                backDate();
                finish();
            }
        });

        currentCity = getIntent().getStringExtra("region");
        initOverlay();
        hotCityInit();
        hisCityInit();
    }

    private void searchRegion() {
        String keysword = sh.getText().toString();
        PhoneLiveApi.getkeywords(keysword, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Toast.makeText(RegionActivity.this, "网络请求出错", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                resultList.setVisibility(View.VISIBLE);
                letterListView.setVisibility(View.GONE);
                personList.setVisibility(View.GONE);
                tv_noresult.setVisibility(View.GONE);
                city_lists.clear();
                JSONArray jsonArray = ApiUtils.arrayCheckIsSuccess(response);
                if (jsonArray!=null&&jsonArray.length()>0){
                    for (int i = 0 ; i < jsonArray.length(); i++){
                        try {
                            JSONObject json = jsonArray.getJSONObject(i);
                            String city = json.getString("city");
                            city_lists.add(city);
                            if(city_lists.size() <= 0){
                                tv_noresult.setVisibility(View.VISIBLE);
                                Toast.makeText(RegionActivity.this, "请输入有效的位置", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    resultListAdapter = new ResultListAdapter(RegionActivity.this,RegionActivity.this.getLayoutInflater(), city_lists);
                    resultList.setAdapter(resultListAdapter);
                }else{
                    Toast.makeText(RegionActivity.this, "请输入有效的位置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void backDate() {
        Bundle bundle = new Bundle();
        bundle.putString("region", currentCity);
        setResult(0, this.getIntent().putExtras(bundle));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            backDate();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void cityInit() {
        City city = new City("定位", "0"); // 当前定位城市
        allCity_lists.add(city);
        city = new City("最近", "1"); // 最近访问的城市
        allCity_lists.add(city);
        city = new City("热门", "2"); // 热门城市
        allCity_lists.add(city);
        city = new City("全部", "3"); // 全部城市
        allCity_lists.add(city);
        getCityList();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /**
     * 热门城市
     */
    public void hotCityInit() {
        City city = new City("全国", "2");
        city_hot.add(city);
        city = new City("上海市", "2");
        city_hot.add(city);
        city = new City("北京市", "2");
        city_hot.add(city);
        city = new City("广州市", "2");
        city_hot.add(city);
        city = new City("深圳市", "2");
        city_hot.add(city);
        city = new City("武汉市", "2");
        city_hot.add(city);
        city = new City("天津市", "2");
        city_hot.add(city);
        city = new City("西安市", "2");
        city_hot.add(city);
        city = new City("南京市", "2");
        city_hot.add(city);
        city = new City("杭州市", "2");
        city_hot.add(city);
        city = new City("成都市", "2");
        city_hot.add(city);
        city = new City("重庆市", "2");
        city_hot.add(city);
    }

    private void hisCityInit() {
        UserBean user = AppContext.getInstance().getLoginUser();
        String id = user.getId()+"";
        PhoneLiveApi.getevercity(id , new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Toast.makeText(RegionActivity.this, "网络请求出错", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONArray array = ApiUtils.arrayCheckIsSuccess(response);
                if (array!=null){
                    for (int i = 0 ; i < array.length(); i++){
                        try {
                            JSONObject json = array.getJSONObject(i);
                            String city = json.getString("city");
                            city_history.add(city);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    cityInit();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void getCityList() {
        PhoneLiveApi.getcity(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Toast.makeText(RegionActivity.this, "网络请求出错", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONArray res = ApiUtils.arrayCheckIsSuccess(response);
                ArrayList<City> list = new ArrayList();
                if (res != null) {
                    try {
                        for ( int i = 0; i < res.length(); i++) {
                            JSONArray Array1 = res.getJSONArray(i);
                            for ( int j = 0; j < Array1.length(); j++) {
                                if (Array1.length()!=0) {
                                    JSONObject js = Array1.getJSONObject(j);
                                    String city = js.getString("city");
                                    String fword = js.getString("fword");
                                    City c = new City(city, fword);
                                    list.add(c);
                                }
                            }
                        }
                        Collections.sort(list, comparator);
                        allCity_lists.addAll(list);
                        setAdapter(allCity_lists, city_hot, city_history);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /**
     * a-z排序
     */
    @SuppressWarnings("rawtypes")
    Comparator comparator = new Comparator<City>() {
        @Override
        public int compare(City lhs, City rhs) {
            String a = lhs.getPinyi().substring(0, 1);
            String b = rhs.getPinyi().substring(0, 1);
            int flag = a.compareTo(b);
            if (flag == 0) {
                return a.compareTo(b);
            } else {
                return flag;
            }
        }
    };

    private void setAdapter(List<City> list, List<City> hotList,
                            List<String> hisCity) {
        adapter = new ListAdapter(this, list, hotList, hisCity);
        personList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }
    @Override
    protected boolean hasActionBar() {
        return false;
    }

    protected void onAMapLocationChanged(AMapLocation amapLocation) {
        AppContext.address = amapLocation.getCity();
    }

    private class ResultListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList results = new ArrayList();

        public ResultListAdapter(Context context, LayoutInflater layoutInflater, ArrayList results) {
            inflater = layoutInflater;
            this.results = results;
        }

        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView
                        .findViewById(R.id.name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.name.setText(results.get(position).toString());
            return convertView;
        }



        class ViewHolder {
            TextView name;
        }
    }

    public class ListAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<City> list;
        private List<City> hotList;
        private List<String> hisCity;
        final int VIEW_TYPE = 5;

        public ListAdapter(Context context, List<City> list,
                           List<City> hotList, List<String> hisCity) {
            this.inflater = LayoutInflater.from(context);
            this.list = list;
            this.context = context;
            this.hotList = hotList;
            this.hisCity = hisCity;
            alphaIndexer = new HashMap<String, Integer>();
            sections = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                // 当前汉语拼音首字母
                String currentStr = getAlpha(list.get(i).getPinyi());
                // 上一个汉语拼音首字母，如果不存在为" "
                String previewStr = (i - 1) >= 0 ? getAlpha(list.get(i - 1)
                        .getPinyi()) : " ";
                if (!previewStr.equals(currentStr)) {
                    String name = getAlpha(list.get(i).getPinyi());
                    alphaIndexer.put(name, i);
                    sections[i] = name;
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE;
        }

        @Override
        public int getItemViewType(int position) {
            return position < 4 ? position : 4;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        ViewHolder holder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView city;
            int viewType = getItemViewType(position);
            if (viewType == 0) { // 定位
                convertView = inflater.inflate(R.layout.frist_list_item, null);
                final TextView locateHint = (TextView) convertView
                        .findViewById(R.id.locateHint);
                city = (TextView) convertView.findViewById(R.id.lng_city);
                final ProgressBar pbLocate = (ProgressBar) convertView
                        .findViewById(R.id.pbLocate);
                city.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        city.setText("");
                        pbLocate.setVisibility(View.VISIBLE);
                        UserBean userBean =  AppContext.getInstance().getLoginUser();
                        PhoneLiveApi.getMyUserInfo(userBean.getId(), userBean.getToken(), new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e) {
                                pbLocate.setVisibility(View.GONE);
                                locateHint.setText("未定位到城市,请选择");
                                city.setVisibility(View.VISIBLE);
                                city.setText("重新选择");
                            }

                            @Override
                            public void onResponse(String response) {
                                String res = ApiUtils.checkIsSuccess(response);
                                if(res == null){
                                    UIHelper.showLoginSelectActivity(RegionActivity.this);
                                    finish();
                                    return;
                                }
                                UserBean mInfo = new Gson().fromJson(res,UserBean.class);
                                String dingweicity = mInfo.getCity_1();
                                if (dingweicity !=null){
                                    pbLocate.setVisibility(View.GONE);
                                    locateHint.setText("当前定位城市");
                                    city.setText(dingweicity);
                                    currentCity = dingweicity;
                                    backDate();
                                    finish();
                                }else {
                                    pbLocate.setVisibility(View.GONE);
                                    locateHint.setText("未定位到城市,请选择");
                                    city.setVisibility(View.VISIBLE);
                                    city.setText("重新选择");
                                }
                            }
                        });
                    }
                });
            } else if (viewType == 1) { // 最近访问城市
                convertView = inflater.inflate(R.layout.recent_city, null);
                GridView rencentCity = (GridView) convertView
                        .findViewById(R.id.recent_city);
                rencentCity
                        .setAdapter(new HitCityAdapter(context, this.hisCity));
                rencentCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        currentCity = city_history.get(position);
                        backDate();
                        finish();

                    }
                });
                TextView recentHint = (TextView) convertView
                        .findViewById(R.id.recentHint);
                recentHint.setText("最近访问的城市");
            } else if (viewType == 2) {
                convertView = inflater.inflate(R.layout.recent_city, null);
                GridView hotCity = (GridView) convertView
                        .findViewById(R.id.recent_city);
                hotCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        currentCity = city_hot.get(position).getName();
                        backDate();
                        finish();
                    }
                });
                hotCity.setAdapter(new HotCityAdapter(context, this.hotList));
                TextView hotHint = (TextView) convertView
                        .findViewById(R.id.recentHint);
                hotHint.setText("热门城市");
            } else if (viewType == 3) {
                convertView = inflater.inflate(R.layout.total_item, null);
            } else {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.list_item, null);
                    holder = new ViewHolder();
                    holder.alpha = (TextView) convertView
                            .findViewById(R.id.alpha);
                    holder.name = (TextView) convertView
                            .findViewById(R.id.name);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                if (position >= 1) {
                    holder.name.setText(list.get(position).getName());
                    String currentStr = getAlpha(list.get(position).getPinyi());
                    String previewStr = (position - 1) >= 0 ? getAlpha(list
                            .get(position - 1).getPinyi()) : " ";
                    if (!previewStr.equals(currentStr)) {
                        holder.alpha.setVisibility(View.VISIBLE);
                        holder.alpha.setText(currentStr);
                    } else {
                        holder.alpha.setVisibility(View.GONE);
                    }
                }
            }
            return convertView;
        }

        private class ViewHolder {
            TextView alpha; // 首字母标题
            TextView name; // 城市名字
        }
    }
    @Override
    protected void onStop() {
        //mLocationClient.stopLocation();
        super.onStop();
    }

    class HotCityAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<City> hotCitys;

        public HotCityAdapter(Context context, List<City> hotCitys) {
            this.context = context;
            inflater = LayoutInflater.from(this.context);
            this.hotCitys = hotCitys;
        }

        @Override
        public int getCount() {
            return hotCitys.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_city, null);
            TextView city = (TextView) convertView.findViewById(R.id.city);
            city.setText(hotCitys.get(position).getName());
            return convertView;
        }
    }

    class HitCityAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private List<String> hotCitys;

        public HitCityAdapter(Context context, List<String> hotCitys) {
            this.context = context;
            inflater = LayoutInflater.from(this.context);
            this.hotCitys = hotCitys;
        }

        @Override
        public int getCount() {
            return hotCitys.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.item_city, null);
            TextView city = (TextView) convertView.findViewById(R.id.city);
            city.setText(hotCitys.get(position));
            return convertView;
        }
    }

    private boolean mReady;

    // 初始化汉语拼音首字母弹出提示框
    private void initOverlay() {
        mReady = true;
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(R.layout.overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        WindowManager windowManager = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, lp);
    }

    private boolean isScroll = false;

    private class LetterListViewListener implements
            MyLetterListView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            isScroll = false;
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                personList.setSelection(position);
                overlay.setText(s);
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                // 延迟一秒后执行，让overlay为不可见
                handler.postDelayed(overlayThread, 1000);
            }
        }
    }

    // 设置overlay不可见
    private class OverlayThread implements Runnable {
        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
        }
    }

    // 获得汉语拼音首字母
    private String getAlpha(String str) {
        if (str == null) {
            return "#";
        }
        if (str.trim().length() == 0) {
            return "#";
        }
        char c = str.trim().substring(0, 1).charAt(0);
        // 正则表达式，判断首字母是否是英文字母
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c + "").matches()) {
            return (c + "").toUpperCase();
        } else if (str.equals("0")) {
            return "定位";
        } else if (str.equals("1")) {
            return "最近";
        } else if (str.equals("2")) {
            return "热门";
        } else if (str.equals("3")) {
            return "全部";
        } else {
            return "#";
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL
                || scrollState == SCROLL_STATE_FLING) {
            isScroll = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (!isScroll) {
            return;
        }

        if (mReady) {
            String text;
            String name = allCity_lists.get(firstVisibleItem).getName();
            String pinyin = allCity_lists.get(firstVisibleItem).getPinyi();
            if (firstVisibleItem < 4) {
                text = name;
            } else {
                text = PingYinUtil.converterToFirstSpell(pinyin)
                        .substring(0, 1).toUpperCase();
            }
            overlay.setText(text);
            overlay.setVisibility(View.VISIBLE);
            handler.removeCallbacks(overlayThread);
            // 延迟一秒后执行，让overlay为不可见
            handler.postDelayed(overlayThread, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(overlayThread);
    }

    @Override
    protected void onDestroy() {
        OkHttpUtils.getInstance().cancelTag("getcity");
        OkHttpUtils.getInstance().cancelTag("getevercity");
        OkHttpUtils.getInstance().cancelTag("getkeywords");
        super.onDestroy();
    }
}
