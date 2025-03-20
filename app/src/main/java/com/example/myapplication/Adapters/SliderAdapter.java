package com.example.myapplication.Adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.example.myapplication.R;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    // Arrays for images, headings, and descriptions
    int[] imagesArray = {
            R.drawable.onboardingscreen1,
            R.drawable.onboardingscreen2,
            R.drawable.onboardingscreen3
    };

    int[] headingArray = {
            R.string.first_slide,
            R.string.second_slide,
            R.string.third_slide
    };

    int[] descriptionArray = {
            R.string.description1,
            R.string.description2,
            R.string.description3
    };

    // Listener for button clicks
    private OnButtonClickListener buttonClickListener;

    public interface OnButtonClickListener {
        void onNextClick(int position);
        void onSignInClick();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.buttonClickListener = listener;
    }

    public SliderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return headingArray.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.sliding_layout, container, false);

        // Find views in the layout
        ImageView imageView = view.findViewById(R.id.slider_img);
        TextView heading = view.findViewById(R.id.heading);
        TextView description = view.findViewById(R.id.description);
        LinearLayout dotsIndicator = view.findViewById(R.id.dots);
        TextView nextButton = view.findViewById(R.id.next_button);
        TextView signInButton = view.findViewById(R.id.sign_in_button);

        // Set data for the current slide
        imageView.setImageResource(imagesArray[position]);
        heading.setText(headingArray[position]);
        description.setText(descriptionArray[position]);

        // Update dots indicator for the current position
        addDots(position, dotsIndicator);

        // Set click listeners for buttons
        nextButton.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onNextClick(position);
            }
        });

        signInButton.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onSignInClick();
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private void addDots(int position, LinearLayout dotsIndicator) {
        TextView[] dots = new TextView[3];
        dotsIndicator.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(context);
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setPadding(20, 0, 20, 0);
            dots[i].setTextSize(35);
            dots[i].setTextColor(ContextCompat.getColor(context, i == position ? R.color.blue : android.R.color.darker_gray));
            dotsIndicator.addView(dots[i]);
        }
    }
}