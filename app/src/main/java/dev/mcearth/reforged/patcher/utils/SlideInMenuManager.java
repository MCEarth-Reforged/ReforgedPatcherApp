package dev.mcearth.reforged.patcher.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.fragment.app.FragmentManager;
import dev.mcearth.reforged.patcher.R;

public class SlideInMenuManager {
    private Context context;
    private FragmentManager fragmentManager;
    private View menuView;
    private View overlayView;
    private FrameLayout menuContainer;
    private boolean isMenuOpen = false;

    public SlideInMenuManager(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    public void createMenu(FrameLayout rootContainer) {
        // Create overlay for dimming background
        overlayView = new View(context);
        overlayView.setBackgroundColor(0x80000000);
        overlayView.setVisibility(View.GONE);
        overlayView.setOnClickListener(v -> closeMenu());
        
        // Inflate menu layout
        LayoutInflater inflater = LayoutInflater.from(context);
        menuView = inflater.inflate(R.layout.slide_in_menu, null);
        
        // Create container for menu
        menuContainer = new FrameLayout(context);
        menuContainer.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        // Add views to container
        menuContainer.addView(overlayView, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.Gravity.END
        );
        menuContainer.addView(menuView, menuParams);
        
        // Initially position menu off-screen
        menuView.setTranslationX(menuView.getWidth());
        
        // Add to root container
        rootContainer.addView(menuContainer);
        
        // Setup click listeners
        setupMenuClickListeners();
    }

    private void setupMenuClickListeners() {
        // Close button
        menuView.findViewById(R.id.closeMenu).setOnClickListener(v -> closeMenu());
        
        // Settings item
        menuView.findViewById(R.id.settingsItem).setOnClickListener(v -> {
            closeMenu();
            // Open settings activity
            context.startActivity(new android.content.Intent(context, dev.mcearth.reforged.patcher.SettingsActivity.class));
        });
        
        // About item
        menuView.findViewById(R.id.aboutItem).setOnClickListener(v -> {
            closeMenu();
            // Open about activity
            context.startActivity(new android.content.Intent(context, dev.mcearth.reforged.patcher.AboutActivity.class));
        });
        
        // Server settings item
        menuView.findViewById(R.id.serverItem).setOnClickListener(v -> {
            closeMenu();
            // Open settings activity (for server settings)
            context.startActivity(new android.content.Intent(context, dev.mcearth.reforged.patcher.SettingsActivity.class));
        });
    }

    public void toggleMenu() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    public void openMenu() {
        if (!isMenuOpen && menuView != null) {
            overlayView.setVisibility(View.VISIBLE);
            menuView.animate()
                .translationX(0)
                .setDuration(300)
                .withStartAction(() -> {
                    menuView.setVisibility(View.VISIBLE);
                    menuView.setTranslationX(menuView.getWidth());
                })
                .start();
            isMenuOpen = true;
        }
    }

    public void closeMenu() {
        if (isMenuOpen && menuView != null) {
            menuView.animate()
                .translationX(menuView.getWidth())
                .setDuration(300)
                .withEndAction(() -> {
                    overlayView.setVisibility(View.GONE);
                })
                .start();
            isMenuOpen = false;
        }
    }

    public boolean isMenuOpen() {
        return isMenuOpen;
    }
}
