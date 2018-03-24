package com.lifekau.android.lifekau;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.CookieCache;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;

import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class PersistentLoadCookieJar extends PersistentCookieJar {
    private SetCookieCache mCache;
    private CookiePersistor mPersistor;

    public PersistentLoadCookieJar(SetCookieCache cache, CookiePersistor persistor) {
        super(cache, persistor);
        mCache = cache;
        mPersistor = persistor;
    }

    public List<Cookie> LoadCookies(){
        List<Cookie> cookies = mPersistor.loadAll();
        Iterator<Cookie> iter = mCache.iterator();
        while(iter.hasNext()){
            cookies.add(iter.next());
        }
        return cookies;
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        mCache.addAll(cookies);
        mPersistor.saveAll(cookies);
    }
}
