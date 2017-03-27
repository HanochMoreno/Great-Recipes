package com.hanoch.greatrecipes.view;

import android.app.ProgressDialog;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hanoch.greatrecipes.R;

/**
 * Fragment to show the recipe url
 */
public class WebViewFragment extends Fragment {

    private static final String ARG_EXTRA_URL = "url";
    private WebView webView;

//-------------------------------------------------------------------------------------------------

    public static WebViewFragment newInstance(String url) {

        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_URL, url);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        webView = (WebView) view.findViewById(R.id.webView);
        String url = getArguments().getString(ARG_EXTRA_URL);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);

        if (savedInstanceState==null) {

            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle(getString(R.string.setting_website));
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setMax(100);

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    progressDialog.show();
                }
            }, 600);

            webView.setWebChromeClient(new WebChromeClient() {

                public void onProgressChanged(WebView view, int progress) {

                    progressDialog.setProgress(progress);

                    if (progress > 85) {
                        progressDialog.dismiss();
                        //webView.setVisibility(View.VISIBLE);
                    }
                }

            });

            webView.setWebViewClient(new WebViewClient() {

                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                    progressDialog.dismiss();
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    progressDialog.dismiss();
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    progressDialog.dismiss();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);

                    return true;
                }
            });

            webView.loadUrl(url);

        } else {

            webView.restoreState(savedInstanceState);
        }

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        webView.saveState(outState);
    }

}
