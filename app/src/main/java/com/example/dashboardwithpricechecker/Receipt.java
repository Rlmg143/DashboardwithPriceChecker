package com.example.dashboardwithpricechecker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

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
    //    private Button btnPrintReceipt;
    private Button btnResetReceipt;
    private String stringQuery = "";
    List<Item> cartContents;
    private TextView subTotalLbl;
    private double subTotal = 0;
    //PRODUCTS/ORDER LISTS
    private final ArrayList<ContentValues> products = new ArrayList<>();
    public static final String PRODUCT_NAME = "PROD_NAME";
    public static final String PRODUCT_COST = "PROD_COST";
    public static final String PRODUCT_QUANTITY = "PROD_QUANTITY";

    private TextView search;
    LinearLayout cart;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cartContents = PrefConfig.readListFromPref(getActivity().getApplicationContext());
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_receipt, container, false);
//        PrefConfig.writeListInPref(getActivity().getApplicationContext(), new ArrayList<>());
        cart = rootView.findViewById(R.id.cart);
        subTotalLbl = rootView.findViewById(R.id.subtotal_fReceipt);

        fetchCart(cart);

        subTotalLbl.setText("₱" + new DecimalFormat("0.00").format(subTotal));


//        btnPrintReceipt = rootView.findViewById(R.id.btn_print_receipt);
//        btnPrintReceipt.setOnClickListener(v -> {
//            if (products.size() > 0){
//                printReceipt();
//            } else {
//                Toast.makeText(getActivity().getApplicationContext(),"Receipt is empty.",Toast.LENGTH_LONG).show();
//            }
//
//        });

        btnResetReceipt = rootView.findViewById(R.id.resetreceipt);
        btnResetReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PrefConfig.writeListInPref(getActivity().getApplicationContext(), new ArrayList<>());
                cart.removeAllViews();
                subTotalLbl.setText("₱0.0");
            }
        });

        search = rootView.findViewById(R.id.search_fReceipt);
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stringQuery = s.toString();
                fetchCart(cart);
//                for (int i = 0; i < cartContents.size(); i++) {
//                    if (cartContents.get(i).getName().contains(query)){
//                        System.out.println(cartContents.get(i).getName());
//                    }
//                }
            }
        });
        return rootView;
    }

    private void fetchCart(LinearLayout cart) {
        cart.removeAllViews();
        try {
            if (stringQuery.isEmpty()) {
                for (int i = 0; i < cartContents.size(); i++) {
                    subTotal += Double.parseDouble(cartContents.get(i).getPrice());
                    createReceiptItem(cart, i);
                }
            } else {
                for (int i = 0; i < cartContents.size(); i++) {
                    subTotal += Double.parseDouble(cartContents.get(i).getPrice());
                    if (cartContents.get(i).getName().contains(stringQuery)) {
                        createReceiptItem(cart, i);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void createReceiptItem(LinearLayout cart, int i) {
        LinearLayout row = new LinearLayout(getActivity().getApplicationContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(5, 0, 8, 20);
        row.setLayoutParams(rowParams);

        LinearLayout cardView = createItemCard(cartContents.get(i).getName(), cartContents.get(i).getPrice(), "Food", cartContents.get(i).getImg(), "" + cartContents.get(i).getQuantity(), "");
        row.addView(cardView);

        cart.addView(row);

        ContentValues cartItem = new ContentValues();
        cartItem.put(PRODUCT_NAME, cartContents.get(i).getName());
        cartItem.put(PRODUCT_COST, cartContents.get(i).getPrice());
        cartItem.put(PRODUCT_QUANTITY, cartContents.get(i).getQuantity());
        products.add(cartItem);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        deleteRowButton = view.findViewById(R.id.resetreceipt);
//        deleteRowButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int rowCount = tableLayout.getChildCount();
//                for (int i = 1; i < rowCount; i++) {
//                    View rowView = tableLayout.getChildAt(i);
//                    if (rowView instanceof TableRow) {
//                        TableRow tableRow = (TableRow) rowView;
//                        int childCount = tableRow.getChildCount();
//                        for (int j = 0; j < childCount; j++) {
//                            View childView = tableRow.getChildAt(j);
//                            if (childView instanceof TextView) {
//                                ((TextView) childView).setText("");
//                            }
//                        }
//                    }
//                }
//            }
//        });
//
//        tableLayout = view.findViewById(R.id.receipttable);
//        TableRow tablerow = view.findViewById(R.id.tablerow);
//
//        // Remove the TableRow from its current parent view, if any
//        ViewGroup parent = (ViewGroup) tablerow.getParent();
//        if (parent != null) {
//            parent.removeView(tablerow);
//        }
//
//        tableLayout.addView(tablerow);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove the TableRow from the TableLayout, if it's still there
//        if (tableLayout != null) {
//            TableRow tablerow = getView().findViewById(R.id.tablerow);
//            if (tablerow != null) {
//                tableLayout.removeView(tablerow);
//            }
//        }
    }

    public void printReceipt() {
// FOR BLUETOOTH PERMISSION
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH}, 1);
        } else if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
//
//        for(BluetoothConnection b : new BluetoothConnections().getList()) {
//            Log.d("BLUETOOTH", b.getDevice().getName());
//        }

        BluetoothConnection[] connections = new BluetoothConnections().getList();
        String[] strCons = new String[connections.length];
        for (int i = 0; i < connections.length; i++) {
            strCons[i] = connections[i].getDevice().getName();
        }

        new AlertDialog.Builder(getActivity())
                .setTitle("Select bluetooth connection")
                .setItems(strCons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            BluetoothConnection connection = connections[which].connect();

                            EscPosPrinter printer = new EscPosPrinter(connection, 203, 48f, 32);

                            StringBuilder sb = new StringBuilder();
                            double total = 0;

                            sb.append("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, getActivity().getApplicationContext().getResources().getDrawableForDensity(R.drawable.zantualogo, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n");
                            sb.append("[C] " + new SimpleDateFormat("EEE yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()) + "\n\n");
                            for (ContentValues cv : products) {
                                sb.append("[L] " + shortenText(cv.getAsString(PRODUCT_NAME)) + "\t P" + cv.getAsDouble(PRODUCT_COST) + "\n");
                                total += cv.getAsDouble(PRODUCT_COST);
                            }
                            sb.append("[C] \n-------------------------\n");
                            sb.append("[L] Total: \t" + total);
                            sb.append("[C]\n");
                            sb.append("[C]\n");
                            sb.append("[C]\n");
                            sb.append("[C] Thanks for supporting\n");
                            sb.append("[C] local business\n");
                            printer.printFormattedText(sb.toString());
                        } catch (EscPosConnectionException e) {
                            Log.d("ERROR", e.getMessage());
                            throw new RuntimeException(e);
                        } catch (EscPosEncodingException e) {
                            Log.d("ERROR", e.getMessage());
                            throw new RuntimeException(e);
                        } catch (EscPosBarcodeException e) {
                            Log.d("ERROR", e.getMessage());
                            throw new RuntimeException(e);
                        } catch (EscPosParserException e) {
                            Log.d("ERROR", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                })
                .create()
                .show();


// FOR BLUETOOTH THERMAL PRINTING
//        BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();
//        try {
//            EscPosPrinter printer = new EscPosPrinter(connection, 203, 48f, 32);
//
        // FORMAT PRINTING
//            String test = "[C] Putaena mo kev!";
//            printer.printFormattedText(test);
//        } catch (EscPosConnectionException e) {
//            Log.d("ERROR", e.getMessage());
//            throw new RuntimeException(e);
//        } catch (EscPosEncodingException e) {
//            Log.d("ERROR", e.getMessage());
//            throw new RuntimeException(e);
//        } catch (EscPosBarcodeException e) {
//            Log.d("ERROR", e.getMessage());
//            throw new RuntimeException(e);
//        } catch (EscPosParserException e) {
//            Log.d("ERROR", e.getMessage());
//            throw new RuntimeException(e);
//        }
    }

    public String shortenText(String str) {
        if (str.length() > 10) {
            return str.substring(0, 10) + "\n" + str.substring(10);
        }

        return str;
    }

    private LinearLayout createItemCard(String name, String price, String categ, String imageUrl, String prodQuantity, String data) {

        LinearLayout cardContainer = new LinearLayout(getActivity().getApplicationContext());
        cardContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        CardView cardview = new CardView(getActivity());

        CardView.LayoutParams layoutparams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );

        layoutparams.setMargins(20, 10, 10, 2);

        cardview.setLayoutParams(layoutparams);
        cardview.setRadius(15);
        cardview.setPadding(25, 25, 25, 25);
        cardview.setMaxCardElevation(30);
        cardview.setMaxCardElevation(6);


        LinearLayout l1 = new LinearLayout(getActivity().getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(20, 20, 20, 20);
        l1.setOrientation(LinearLayout.HORIZONTAL);
        l1.setLayoutParams(params);


        ImageView image = new ImageView(getActivity().getApplicationContext());
        image.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        Glide.with(getActivity()).load(imageUrl).into(image);

        l1.addView(image);

        LinearLayout l2 = new LinearLayout(getActivity().getApplicationContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 0,0,0);
        l2.setLayoutParams(layoutParams);
        l2.setOrientation(LinearLayout.VERTICAL);

        l1.addView(l2);

        TextView productName = new TextView(getActivity().getApplicationContext());
        productName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        productName.setText(name);
        productName.setTypeface(null, Typeface.BOLD);
        productName.setTextSize(20);

        l2.addView(productName);

        TextView category = new TextView(getActivity().getApplicationContext());
        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        categoryParams.setMargins(0, 8, 0, 8);
        category.setLayoutParams(categoryParams);
        category.setText("FOOD");
        category.setTextSize(12);
        category.setBackgroundResource(R.drawable.round_corners);
        category.setTextColor(Color.WHITE);
        l2.addView(category);

        TextView productRetailPrice = new TextView(getActivity().getApplicationContext());
        productRetailPrice.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        productRetailPrice.setText("Total: ₱" + price);
        productRetailPrice.setTextSize(15);
        l2.addView(productRetailPrice);

        LinearLayout l3 = new LinearLayout(getActivity().getApplicationContext());

        LinearLayout.LayoutParams l3Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        l3Params.gravity = Gravity.CENTER;
        l3.setLayoutParams(l3Params);
        l3.setOrientation(LinearLayout.HORIZONTAL);

//        l1.addView(l3);

        TextView quantity = new TextView(getActivity().getApplicationContext());
        ImageView removeIcon = new ImageView(getActivity().getApplicationContext());
        removeIcon.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
        removeIcon.setImageResource(R.drawable.ic_baseline_remove_circle_24);

        double origPrice = Double.parseDouble(price) / Integer.parseInt(prodQuantity);
        removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(quantity.getText().toString()) > 1) {
                    int newQty = Integer.parseInt(quantity.getText().toString()) - 1;
                    double newPrice = newQty * origPrice;
                    quantity.setText("" + newQty);
                    productRetailPrice.setText("Price: ₱" + newPrice);

                }

            }
        });

        l3.addView(removeIcon);

        quantity.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        quantity.setText(prodQuantity);
        quantity.setTextSize(24);
        l3.addView(quantity);

        ImageView plusIcon = new ImageView(getActivity().getApplicationContext());
        LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(70, 70);
        plusIcon.setLayoutParams(plusParams);
        plusIcon.setImageResource(R.drawable.ic_baseline_add_circle_24);
        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQty = Integer.parseInt(quantity.getText().toString()) + 1;
                double newPrice = newQty * origPrice;
                quantity.setText("" + newQty);
                productRetailPrice.setText("Price: ₱" + newPrice);
            }
        });
        l3.addView(plusIcon);

        final String productData = data;
        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ProductDetails.class);
                intent.putExtra("productDetails", productData);
                startActivity(intent);
            }
        });
        cardview.addView(l1);

//        TextView test = new TextView(getActivity().getApplicationContext());
//        test.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        test.setText("Testasdasdasdasdasd");
//        test.setTypeface(null, Typeface.BOLD);
//        cardContainer.addView(l3);

        cardContainer.addView(cardview);
        return cardContainer;
    }

}


