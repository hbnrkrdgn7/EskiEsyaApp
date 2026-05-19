package com.example.eskiesyasatis;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvTotalAmount;
    private Spinner spinnerSavedCards;
    private LinearLayout layoutNewCard;
    private EditText etCardNumber, etCardHolder, etExpiryDate, etCvv;
    private CheckBox cbSaveCard;
    private MaterialButton btnCompletePayment;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Card> savedCardsList = new ArrayList<>();
    private List<String> spinnerItems = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private double totalAmount = 0;
    
    private static final String ADD_NEW_CARD_TEXT = "Yeni Kart Ekle...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        totalAmount = getIntent().getDoubleExtra("totalAmount", 0);

        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        spinnerSavedCards = findViewById(R.id.spinnerSavedCards);
        layoutNewCard = findViewById(R.id.layoutNewCard);
        etCardNumber = findViewById(R.id.etCardNumber);
        etCardHolder = findViewById(R.id.etCardHolder);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCvv = findViewById(R.id.etCvv);
        cbSaveCard = findViewById(R.id.cbSaveCard);
        btnCompletePayment = findViewById(R.id.btnCompletePayment);
        ImageView btnBack = findViewById(R.id.btnBack);

        android.widget.RadioGroup rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        LinearLayout layoutCardDetails = findViewById(R.id.layoutCardDetails);

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCash) {
                layoutCardDetails.setVisibility(View.GONE);
            } else {
                layoutCardDetails.setVisibility(View.VISIBLE);
            }
        });

        btnBack.setOnClickListener(v -> finish());
        tvTotalAmount.setText(totalAmount + " TL");

        setupSpinner();
        loadSavedCards();
        setupTextWatchers();

        btnCompletePayment.setOnClickListener(v -> processPayment());
    }

    private void setupTextWatchers() {
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private boolean deletingHyphen;
            private int hyphenStart;
            private boolean deletingBackward;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting) return;
                deletingBackward = count > after;
                if (deletingBackward && s.charAt(start) == ' ') {
                    deletingHyphen = true;
                    hyphenStart = start;
                } else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                if (deletingHyphen && hyphenStart > 0) {
                    if (deletingBackward) {
                        if (hyphenStart - 1 < s.length()) {
                            s.delete(hyphenStart - 1, hyphenStart);
                        }
                    }
                }

                String cleanString = s.toString().replaceAll(" ", "");
                StringBuilder formattedString = new StringBuilder();
                for (int i = 0; i < cleanString.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formattedString.append(" ");
                    }
                    formattedString.append(cleanString.charAt(i));
                }

                s.replace(0, s.length(), formattedString.toString());
                isFormatting = false;
            }
        });

        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");
                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;
                    if (clean.length() < 5) {
                        String m = "";
                        String y = "";
                        if (clean.length() >= 2) {
                            m = clean.substring(0, 2);
                            y = clean.substring(2);
                        } else {
                            m = clean;
                        }
                        if (m.length() == 1 && Integer.parseInt(m) > 1) {
                            m = "0" + m;
                        } else if (m.length() == 2 && Integer.parseInt(m) > 12) {
                            m = "12";
                        }
                        clean = String.format("%s%s%s", m, m.length() == 2 && clean.length() > 2 ? "/" : m.length() == 2 ? "/" : "", y);
                    }
                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    etExpiryDate.setText(current);
                    etExpiryDate.setSelection(sel < current.length() ? sel : current.length());
                }
            }
        });
    }

    private void setupSpinner() {
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerItems) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.TextView) {
                    ((android.widget.TextView) view).setTextColor(0xFF1A1A1A);
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof android.widget.TextView) {
                    ((android.widget.TextView) view).setTextColor(0xFF1A1A1A);
                }
                return view;
            }
        };
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSavedCards.setAdapter(spinnerAdapter);

        spinnerSavedCards.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = spinnerItems.get(position);
                if (selectedItem.equals(ADD_NEW_CARD_TEXT)) {
                    layoutNewCard.setVisibility(View.VISIBLE);
                } else {
                    layoutNewCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSavedCards() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userId).collection("cards")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedCardsList.clear();
                    spinnerItems.clear();
                    spinnerItems.add(ADD_NEW_CARD_TEXT);

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Card card = doc.toObject(Card.class);
                        if (card != null) {
                            card.setId(doc.getId());
                            savedCardsList.add(card);
                            spinnerItems.add(card.getMaskedNumber() + " - " + card.getHolderName());
                        }
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    if (savedCardsList.size() > 0) {
                        spinnerSavedCards.setSelection(1);
                        layoutNewCard.setVisibility(View.GONE);
                    } else {
                        spinnerSavedCards.setSelection(0);
                        layoutNewCard.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Kartlar yüklenirken hata oluştu.", Toast.LENGTH_SHORT).show());
    }

    private void processPayment() {
        android.widget.RadioGroup rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        if (rgPaymentMethod != null && rgPaymentMethod.getCheckedRadioButtonId() == R.id.rbCash) {
            createOrderAndComplete("Elden Teslim");
            return;
        }

        String selectedOption = spinnerItems.get(spinnerSavedCards.getSelectedItemPosition());
        boolean isNewCard = selectedOption.equals(ADD_NEW_CARD_TEXT);

        if (isNewCard) {
            String cardNumber = etCardNumber.getText().toString().trim().replaceAll(" ", "");
            String cardHolder = etCardHolder.getText().toString().trim();
            String expiryDate = etExpiryDate.getText().toString().trim();
            String cvv = etCvv.getText().toString().trim();

            if (cardNumber.length() != 16 || cardHolder.isEmpty() || expiryDate.length() != 5 || cvv.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm kart bilgilerini doğru formatta doldurun.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbSaveCard.isChecked()) {
                new AlertDialog.Builder(this)
                        .setTitle("Kartı Kaydet")
                        .setMessage("Bu kartı sonraki alışverişleriniz için kaydetmek ister misiniz?")
                        .setPositiveButton("Evet", (dialog, which) -> {
                            saveCardToFirestore(cardNumber, cardHolder, expiryDate);
                            createOrderAndComplete("**** " + cardNumber.substring(cardNumber.length() - 4));
                        })
                        .setNegativeButton("Hayır", (dialog, which) -> {
                            createOrderAndComplete("**** " + cardNumber.substring(cardNumber.length() - 4));
                        })
                        .show();
            } else {
                saveCardToFirestore(cardNumber, cardHolder, expiryDate);
                createOrderAndComplete("**** " + cardNumber.substring(cardNumber.length() - 4));
            }
        } else {
            // Kayıtlı kart seçildi
            int cardIndex = spinnerSavedCards.getSelectedItemPosition() - 1;
            Card selectedCard = savedCardsList.get(cardIndex);
            createOrderAndComplete(selectedCard.getMaskedNumber());
        }
    }

    private void saveCardToFirestore(String cardNumber, String holderName, String expiryDate) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        Card newCard = new Card(null, cardNumber, holderName, expiryDate);
        db.collection("Users").document(userId).collection("cards")
                .add(newCard);
    }

    private void createOrderAndComplete(String cardLastFour) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        java.util.List<String> itemNames = new java.util.ArrayList<>();
        String listingTitle = getIntent().getStringExtra("LISTING_TITLE");
        if (listingTitle != null) {
            itemNames.add(listingTitle);
        } else {
            itemNames.add("Sipariş Edilen Ürün");
        }
        
        String firstItemImageUrl = getIntent().getStringExtra("LISTING_IMAGE_URL");

        String orderDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
        
        Order newOrder = new Order(null, orderDate, totalAmount, itemNames, cardLastFour, firstItemImageUrl);

        db.collection("Users").document(userId).collection("orders")
                .add(newOrder)
                .addOnSuccessListener(documentReference -> {
                    android.widget.Toast.makeText(this, "Ödemeniz başarıyla alındı!", android.widget.Toast.LENGTH_LONG).show();
                    
                    String listingId = getIntent().getStringExtra("LISTING_ID");
                    if (listingId != null) {
                        db.collection("Listings").document(listingId).update("status", "sold");
                    }
                    
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> android.widget.Toast.makeText(this, "Sipariş oluşturulurken bir hata oluştu.", android.widget.Toast.LENGTH_SHORT).show());
    }
}
