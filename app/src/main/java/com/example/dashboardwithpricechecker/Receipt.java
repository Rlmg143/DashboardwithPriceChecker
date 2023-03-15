package com.example.dashboardwithpricechecker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Receipt#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Receipt extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TableLayout tableLayout;
    private Button deleteRowButton;

    public Receipt() {
        // Required empty public constructor
    }

    public static Receipt newInstance(String param1, String param2) {
        Receipt fragment = new Receipt();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_receipt, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deleteRowButton = view.findViewById(R.id.resetreceipt);
        deleteRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rowCount = tableLayout.getChildCount();
                for (int i = 1; i < rowCount; i++) {
                    View rowView = tableLayout.getChildAt(i);
                    if (rowView instanceof TableRow) {
                        TableRow tableRow = (TableRow) rowView;
                        int childCount = tableRow.getChildCount();
                        for (int j = 0; j < childCount; j++) {
                            View childView = tableRow.getChildAt(j);
                            if (childView instanceof TextView) {
                                ((TextView) childView).setText("");
                            }
                        }
                    }
                }
            }
        });

        tableLayout = view.findViewById(R.id.receipttable);
        TableRow tablerow = view.findViewById(R.id.tablerow);

        // Remove the TableRow from its current parent view, if any
        ViewGroup parent = (ViewGroup) tablerow.getParent();
        if (parent != null) {
            parent.removeView(tablerow);
        }

        tableLayout.addView(tablerow);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove the TableRow from the TableLayout, if it's still there
        if (tableLayout != null) {
            TableRow tablerow = getView().findViewById(R.id.tablerow);
            if (tablerow != null) {
                tableLayout.removeView(tablerow);
            }
        }
    }
}


