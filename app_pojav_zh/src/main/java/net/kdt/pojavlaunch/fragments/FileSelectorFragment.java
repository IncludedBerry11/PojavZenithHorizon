package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.filelist.FileIcon;
import com.movtery.filelist.FileRecyclerView;
import com.movtery.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.dialog.EditTextDialog;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class FileSelectorFragment extends Fragment {
    public static final String TAG = "FileSelectorFragment";
    public static final String BUNDLE_SELECT_FOLDER = "select_folder";
    public static final String BUNDLE_SELECT_FILE = "select_file";
    public static final String BUNDLE_SHOW_FILE = "show_file";
    public static final String BUNDLE_SHOW_FOLDER = "show_folder";
    public static final String BUNDLE_ROOT_PATH = "root_path";

    private Button mSelectFolderButton, mCreateFolderButton, mRefreshButton;
    private FileRecyclerView mFileRecyclerView;
    private TextView mFilePathView;

    private boolean mSelectFolder = true;
    private boolean mShowFiles = true;
    private boolean mShowFolders = true;
    private String mRootPath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            ? PojavZHTools.DIR_GAME_DEFAULT
            : Environment.getExternalStorageDirectory().getAbsolutePath();


    public FileSelectorFragment() {
        super(R.layout.fragment_files);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();
        if (!mSelectFolder) mSelectFolderButton.setVisibility(View.GONE);
        else mSelectFolderButton.setVisibility(View.VISIBLE);

        mFileRecyclerView.setShowFiles(mShowFiles);
        mFileRecyclerView.setShowFolders(mShowFolders);
        mFileRecyclerView.lockPathAt(new File(mRootPath));
        mFileRecyclerView.setTitleListener((title) -> mFilePathView.setText(removeLockPath(title)));
        mFileRecyclerView.refreshPath();

        mCreateFolderButton.setOnClickListener(v -> {
            EditTextDialog editTextDialog = new EditTextDialog(requireContext(), getString(R.string.folder_dialog_insert_name), null, null, null);
            editTextDialog.setConfirm(view1 -> {
                File folder = new File(mFileRecyclerView.getFullPath(), editTextDialog.getEditBox().getText().toString().replace("/", ""));
                boolean success = folder.mkdir();
                if (success) {
                    mFileRecyclerView.listFileAt(new File(mFileRecyclerView.getFullPath(), editTextDialog.getEditBox().getText().toString().replace("/", "")));
                } else {
                    mFileRecyclerView.refreshPath();
                }

                editTextDialog.dismiss();
            });
            editTextDialog.show();
        });

        mSelectFolderButton.setOnClickListener(v -> {
            ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(mFileRecyclerView.getFullPath().getAbsolutePath()));
            Tools.removeCurrentFragment(requireActivity());
        });

        mRefreshButton.setOnClickListener(v -> mFileRecyclerView.refreshPath());

        mFileRecyclerView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(path));
                Tools.removeCurrentFragment(requireActivity());
            }

            @Override
            public void onItemLongClick(File file, String path) {
            }
        });
    }

    private String removeLockPath(String path) {
        return path.replace(mRootPath, ".");
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mSelectFolder = bundle.getBoolean(BUNDLE_SELECT_FOLDER, mSelectFolder);
        mShowFiles = bundle.getBoolean(BUNDLE_SHOW_FILE, mShowFiles);
        mShowFolders = bundle.getBoolean(BUNDLE_SHOW_FOLDER, mShowFolders);
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
    }

    private void bindViews(@NonNull View view) {
        mSelectFolderButton = view.findViewById(R.id.zh_files_return_button);
        mCreateFolderButton = view.findViewById(R.id.zh_files_add_file_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mFileRecyclerView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);

        view.findViewById(R.id.zh_files_icon).setVisibility(View.GONE);
        view.findViewById(R.id.zh_files_create_folder_button).setVisibility(View.GONE);
        view.findViewById(R.id.zh_files_help_button).setVisibility(View.GONE);

        mSelectFolderButton.setText(getString(R.string.folder_fragment_select));
        mCreateFolderButton.setText(getString(R.string.folder_fragment_create));
        mFileRecyclerView.setFileIcon(FileIcon.FILE);
    }
}
