package com.zalo.trainingmenu.downloader.ui.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FolderChooserDialog extends DialogFragment implements View.OnClickListener, FolderChooserAdapter.OnClickFolderItem {
    public static final String TAG = "FolderChooserDialog";

    private File parentFolder;
    private File[] parentContents;
    private boolean canGoUp = false;

    private FolderCallback mCallback;

    @Override
    public int getTheme() {
        return R.style.DialogDimDisabled;
    }

    private TextView mTitle;
    private TextView mChooseThisButton;
    private ImageView mChooseThisIcon;
    private RecyclerView mRecyclerView;
    private FolderChooserAdapter mAdapter;

    private void bind(View root) {
        mTitle = root.findViewById(R.id.title);
        View closeButton = root.findViewById(R.id.close);
        mChooseThisButton = root.findViewById(R.id.button);
        mChooseThisIcon = root.findViewById(R.id.folder_icon);
        mRecyclerView = root.findViewById(R.id.recycler_view);

        if(closeButton!=null) closeButton.setOnClickListener(this);
        mChooseThisButton.setOnClickListener(this);
    }

    private String mInitialPath = Util.getCurrentDownloadDirectoryPath();

    private String[] getContentsArray() {
        if (parentContents == null) {
            if (canGoUp) {
                return new String[]{".."};
            }
            return new String[]{};
        }
        String[] results = new String[parentContents.length + (canGoUp ? 1 : 0)];
        if (canGoUp) {
            results[0] = "..";
        }
        for (int i = 0; i < parentContents.length; i++) {
            results[canGoUp ? i + 1 : i] = parentContents[i].getName();
        }
        return results;
    }

    private File[] listFiles() {
        File[] contents = parentFolder.listFiles();
        List<File> results = new ArrayList<>();
        if (contents != null) {
            for (File fi : contents) {
                if (fi.isDirectory()) {
                    results.add(fi);
                }
            }
            Collections.sort(results, new FolderSorter());
            return results.toArray(new File[results.size()]);
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.folder_chooser_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind(view);
        mAdapter = new FolderChooserAdapter();
        mAdapter.setOnClickFolderListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        mRecyclerView.setAdapter(mAdapter);

        if(savedInstanceState == null) savedInstanceState = new Bundle();

        if (!savedInstanceState.containsKey("current_path")) {
            savedInstanceState.putString("current_path", mInitialPath);
        }
        parentFolder = new File(savedInstanceState.getString("current_path", File.pathSeparator));
        checkIfCanGoUp();
        parentContents = listFiles();
        mTitle.setText(parentFolder.getName());
        mAdapter.setData(getContentsArray());
    }

    public static FolderChooserDialog newInstance() {
        return new FolderChooserDialog();
    }

/*    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Dialog dialog = new Dialog(getContext());
            dialog.setTitle("Select path");
            dialog.set

            return new DialogCompat();
                    MaterialDialog.Builder(getActivity())
                    .title(R.string.md_error_label)
                    .content(R.string.md_storage_perm_error)
                    .positiveText(android.R.string.ok)
                    .build();
        }
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        if (!savedInstanceState.containsKey("current_path")) {
            savedInstanceState.putString("current_path", mInitialPath);
        }
        parentFolder = new File(savedInstanceState.getString("current_path", File.pathSeparator));
        checkIfCanGoUp();
        parentContents = listFiles();
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(getActivity())
                        .title(parentFolder.getAbsolutePath())
                        .items((CharSequence[]) getContentsArray())
                        .itemsCallback(this)
                        .autoDismiss(false)
                        .onPositive((dialog, which) -> {
                            dismiss();
                            mCallback.onFolderSelection(FolderChooserDialog.this, parentFolder);
                        })
                        .onNegative((materialDialog, dialogAction) -> dismiss())
                        .positiveText(R.string.add_action)
                        .negativeText(android.R.string.cancel);
        return builder.build();
    }*/

    public void onSelection(int i, String s) {
        if (canGoUp && i == 0) {
            parentFolder = parentFolder.getParentFile();
            if (parentFolder!=null&&"/storage/emulated".equals(parentFolder.getAbsolutePath())) {
                parentFolder = parentFolder.getParentFile();
            }
            checkIfCanGoUp();
        } else if(i==0) {
        } else {
            parentFolder = parentContents[canGoUp ? i - 1 : i];
            canGoUp = true;
            if (parentFolder.getAbsolutePath().equals("/storage/emulated")) {
                parentFolder = Environment.getExternalStorageDirectory();
            }
        }
        reload();
    }

    private void checkIfCanGoUp() {
        File parentFile = parentFolder.getParentFile();
        canGoUp = parentFile != null && parentFolder.getParentFile().canWrite();
    }

    private void reload() {
        parentContents = listFiles();
        // change title
        // changed
        //MaterialDialog dialog = (MaterialDialog) getDialog();
        //dialog.setTitle(parentFolder.getAbsolutePath());
        //dialog.setItems((CharSequence[]) getContentsArray());
        mTitle.setText(parentFolder.getName());
        String[] data = getContentsArray();
        mAdapter.setData(data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current_path", parentFolder.getAbsolutePath());
    }

    public FolderChooserDialog setCallback(FolderCallback callback) {
        this.mCallback = callback;
        return this;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close:
                dismiss();
                break;
            case R.id.button:
                String path = parentFolder.getAbsolutePath();
                if(path.isEmpty()||path.equals("/")) parentFolder = Util.getDefaultDirectory();
                Log.d(TAG, "folder chosen: \""+parentFolder.getAbsolutePath()+"\"");
                if(mCallback!=null) mCallback.onFolderSelection(parentFolder);
                else {
                    SharedPreferences.Editor editor = App.getDefaultSharedPreferences().edit();
                    editor.putString("downloadsFolder", parentFolder.getAbsolutePath());
                    editor.apply();
                }
                dismiss();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        mCallback = null;
        mAdapter.destroy();
        super.onDestroyView();
    }

    @Override
    public void onClickFolderItem(int position, String name) {
        Log.d(TAG, "on click folder item :"+name);
        onSelection(position, name);
    }

    public interface FolderCallback {
        void onFolderSelection(@NonNull File folder);
    }

    private static class FolderSorter implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
