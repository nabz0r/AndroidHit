package com.androidhit.lumadrift.sharing;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/** Read-only, grant-scoped access to explicitly created share files; AGPL-3.0. */
public final class ShareContentProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }
    private File resolve(Uri uri) throws FileNotFoundException {
        if (getContext() == null || uri.getPathSegments().size() != 1) throw new FileNotFoundException("Invalid share path");
        String name = uri.getLastPathSegment();
        if (name == null || !name.matches("[a-zA-Z0-9_-]{1,80}\\.(png|json)")) throw new FileNotFoundException("Invalid share file");
        File directory = new File(getContext().getCacheDir(), "shared");
        File file = new File(directory, name);
        try { if (!file.getCanonicalFile().getParentFile().equals(directory.getCanonicalFile()) || !file.isFile()) throw new FileNotFoundException("Missing share file"); }
        catch (IOException failure) { throw new FileNotFoundException("Invalid share path"); }
        return file;
    }
    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) throw new FileNotFoundException("Read only");
        return ParcelFileDescriptor.open(resolve(uri), ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public String getType(Uri uri) { return uri.getLastPathSegment() != null && uri.getLastPathSegment().endsWith(".png") ? "image/png" : "application/json"; }
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] selectionArgs,String sortOrder) {
        try { File f=resolve(uri);String[] columns=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor cursor=new MatrixCursor(columns);Object[] row=new Object[columns.length];for(int i=0;i<columns.length;i++)row[i]=OpenableColumns.SIZE.equals(columns[i])?f.length():OpenableColumns.DISPLAY_NAME.equals(columns[i])?f.getName():null;cursor.addRow(row);return cursor; }
        catch (FileNotFoundException missing) { return null; }
    }
    @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException("Read only");}
    @Override public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException("Read only");}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException("Read only");}
}
