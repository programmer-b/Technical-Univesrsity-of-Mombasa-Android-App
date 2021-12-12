package com.tum.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.net.URLEncoder;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PdfView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PdfView extends Fragment {
    ViewDialog dialog = new ViewDialog();

    WebView webView;
    private AppBarLayout appbar;

    private MaterialToolbar toolbar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PdfView() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PdfView.
     */
    // TODO: Rename and change types and number of parameters
    public static PdfView newInstance(String param1, String param2) {
        PdfView fragment = new PdfView();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pdf_view, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(View view, Bundle savedInstanceState){
        Context context = getContext();
        assert context != null;

        view.findViewById(R.id.back_button).setOnClickListener(view12 -> NavHostFragment.findNavController(PdfView.this)
                .navigate(R.id.ContentToHome));
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    public void WebAction(String URL) {

        appbar = requireView().findViewById(R.id.appbar);
        toolbar = requireView().findViewById(R.id.toolbar);

        webView = (WebView) requireView().findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            private int running =0;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                running ++;
                webView.loadUrl(url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                webView.loadUrl("file:///android_assets/error.html");
                dialog.hideProgress();

            }
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                running = Math.max(running,1);

            }

            public void onPageFinished(WebView view, String url) {
                if (--running == 0) {
                    AndroidUtils.toastMessage(requireContext(), "Loading finished.");
                    dialog.hideProgress();
                } else {
                    Context context = getContext();
                    assert context != null;

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    String URL = sharedPreferences.getString("URL","");

                    WebAction(URL);
                }
            }

        });
        Context context = getContext();
        assert context != null;

        String url="";
        try {
            url = URLEncoder.encode(URL,"UTF-8");
        } catch (Exception e) { }
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+url);
        webView.clearCache(true);
        webView.clearHistory();
    }

    @Override
    public void onStart(){
        super.onStart();

        dialog.showProgress(getActivity(),"opening file",true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        AndroidUtils.toastMessage(getContext(),sharedPreferences.getString("URL",""));
        String url = sharedPreferences.getString("URL","");

        WebAction(url);
    }

    @Override
    public void onStop(){
        super.onStop();
        dialog.hideProgress();

        webView = (WebView) requireView().findViewById(R.id.webView);
        webView.destroy();
    }
}