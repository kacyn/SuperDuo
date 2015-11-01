package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.barcode.BarcodeCaptureActivity;
import it.jaschke.alexandria.data.BookContract;
import it.jaschke.alexandria.services.BookService;


public class AddBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = AddBookFragment.class.getSimpleName();
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 0;
    private View rootView;
    private final String ISBN = "isbn";
    private final String ISBN_CONTENT="isbnContent";
    private final String BOOK_TITLE="bookTitle";
    private final String BOOK_SUBTITLE="bookSubtitle";
    private final String AUTHORS="authors";
    private final String IMG_URL="imgUrl";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    public static final String BarcodeObject = "Barcode";

    public static final int BARCODE_REQUEST = 9001;

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    //book details
    private String mBookTitle;
    private String mBookSubTitle;
    private String mAuthors;
    private String mImgUrl;

    private String mIsbn;

    public AddBookFragment(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ean!=null) {
            outState.putString(ISBN_CONTENT, ean.getText().toString());
        }

        outState.putString(ISBN, mIsbn);
        outState.putString(BOOK_TITLE, mBookTitle);
        outState.putString(BOOK_SUBTITLE, mBookSubTitle);
        outState.putString(AUTHORS, mAuthors);
        outState.putString(IMG_URL, mImgUrl);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String isbn = s.toString();

                Log.v(LOG_TAG, "in after text changed.  isbn: " + isbn);

                if(isValidIsbn(isbn)) {

                    Log.v(LOG_TAG, "isbn valid");

                    Log.v(LOG_TAG, "adding book to list");

                    addBookToList(isbn);
                }
            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.

                Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                startActivityForResult(intent, BARCODE_REQUEST);
            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "save button pressed");
                Toast toast = Toast.makeText(getActivity(), "Book Added to List!", Toast.LENGTH_SHORT);
                toast.show();

                ean.setText("");
                clearFields();

            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "delete button pressed");
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mIsbn);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
                clearFields();
            }
        });

        if(savedInstanceState!=null){
            ean.setText(savedInstanceState.getString(ISBN_CONTENT));
            ean.setHint("");

            mIsbn = savedInstanceState.getString(ISBN);
            mBookTitle = savedInstanceState.getString(BOOK_TITLE);
            mBookSubTitle = savedInstanceState.getString(BOOK_SUBTITLE);
            mAuthors = savedInstanceState.getString(AUTHORS);
            mImgUrl = savedInstanceState.getString(IMG_URL);

            restartLoader();
        }

        return rootView;
    }

    //retrieve results from barcode code
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_REQUEST) { // Please, use a final int instead of hardcoded int value
            if (resultCode == CommonStatusCodes.SUCCESS) {

                if(data != null) {
                    Barcode barcode = (Barcode) data.getExtras().get(BarcodeObject);

                    String isbn = barcode.rawValue;
                    Log.v("Barcode", "Barcode result: " + isbn);

                    if(isValidIsbn(isbn)) {
                        mIsbn = isbn;

                        addBookToList(mIsbn);
                    }
                }
            }
        }
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "in oncreateloader");

        Long isbn = 0l;

        if(mIsbn != null) {
            isbn = Long.parseLong(mIsbn);
        }

        return new CursorLoader(
                getActivity(),
                BookContract.BookEntry.buildFullBookUri(isbn),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "in onloadfinished");

        if (!data.moveToFirst()) {
            return;
        }

        mBookTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(mBookTitle);

        mBookSubTitle = data.getString(data.getColumnIndex(BookContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(mBookSubTitle);

        mAuthors = data.getString(data.getColumnIndex(BookContract.AuthorEntry.AUTHOR));
        String[] authorsArr = mAuthors.split(",");
        ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) rootView.findViewById(R.id.authors)).setText(mAuthors.replace(",","\n"));

        mImgUrl = data.getString(data.getColumnIndex(BookContract.BookEntry.IMAGE_URL));

        ImageView bookImageView = (ImageView) rootView.findViewById(R.id.bookCover);
        bookImageView.setVisibility(View.VISIBLE);
        Picasso.with(getActivity()).load(mImgUrl).into(bookImageView);

        String categories = data.getString(data.getColumnIndex(BookContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        Log.v(LOG_TAG, "in clear fields");

        mIsbn = null;
        mBookTitle = null;
        mBookSubTitle = null;
        mAuthors = null;
        mImgUrl = null;

        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    private void addBookToList(String isbn){

        if(Utility.isNetworkAvailable(getActivity())) {
            Log.v(LOG_TAG, "network access");
            //Once we have an ISBN, start a book intent
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.EAN, isbn);
            bookIntent.setAction(BookService.FETCH_BOOK);
            getActivity().startService(bookIntent);
            AddBookFragment.this.restartLoader();
        }
        else {
            Log.v(LOG_TAG, "no network access");
            Toast toast = Toast.makeText(getActivity(), getString(R.string.no_network_access), Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    private boolean isValidIsbn(String isbn) {
        //catch isbn10 numbers
        if(isbn.length()==10 && !isbn.startsWith("978")){
            isbn = "978"+isbn;
        }

        Log.v(LOG_TAG, "in isvalidisbn.  isbn: " + isbn);

        char[] isbnCharArray = isbn.toCharArray();

        int sum = 0;

        if(isbnCharArray.length == 13) {
            Log.v(LOG_TAG, "length is 13");
            for(int i = 0; i < 12; i++) {
                if(i % 2 == 0) {
                    sum += Character.getNumericValue(isbnCharArray[i]); //asuming this is 0..9, not '0'..'9'
                } else {
                    sum += Character.getNumericValue(isbnCharArray[i]) * 3;
                }
            }

            Log.v(LOG_TAG, "sum: " + sum);


            if(Character.getNumericValue(isbnCharArray[12]) == (10 - sum % 10)) {
                Log.v(LOG_TAG, "valid isbn13");
                return true;
            }
        }

        Log.v(LOG_TAG, "invalid isbn");
        return false;
    }
}
