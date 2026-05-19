package com.example.eskiesyasatis;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListenerRegistration unreadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Add extra ~80dp padding at the bottom to prevent BottomAppBar from covering content
            int bottomNavHeight = (int) (80 * getResources().getDisplayMetrics().density);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + bottomNavHeight);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // Remove background for BottomNavigationView to not draw over BottomAppBar
        bottomNav.setBackground(null);
        
        // Disable placeholder item so it can't be clicked
        bottomNav.getMenu().getItem(2).setEnabled(false);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (itemId == R.id.nav_chats) {
                selectedFragment = new ChatsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
        
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddListingActivity.class);
            startActivity(intent);
        });

        // Load correct fragment on first launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Listen for unread messages to update Profile badge
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            unreadListener = FirebaseFirestore.getInstance().collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    int unreadChatsCount = 0;
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Map<String, Long> unreadCounts = (Map<String, Long>) doc.get("unreadCounts");
                        if (unreadCounts != null && unreadCounts.containsKey(currentUserId)) {
                            Long count = unreadCounts.get(currentUserId);
                            if (count != null && count > 0) {
                                unreadChatsCount++;
                            }
                        }
                    }
                    
                    BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_chats);
                    if (unreadChatsCount > 0) {
                        badge.setVisible(true);
                        badge.setNumber(unreadChatsCount);
                        badge.setBackgroundColor(android.graphics.Color.parseColor("#F44336")); // Parlak Kırmızı
                        badge.setBadgeTextColor(android.graphics.Color.WHITE);
                    } else {
                        badge.setVisible(false);
                        badge.clearNumber();
                    }
                });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unreadListener != null) {
            unreadListener.remove();
        }
    }
}