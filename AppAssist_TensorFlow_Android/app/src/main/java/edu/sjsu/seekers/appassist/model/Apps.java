package edu.sjsu.seekers.appassist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Apps implements Parcelable {

    public int appId;
    public String asIn;
    public String brand;
    public String category;
    public String desc_arr;
    public String imUrl;

    @Override
    public String toString() {
        return "Apps{" +
                "appId=" + appId +
                ", asIn='" + asIn + '\'' +
                ", brand='" + brand + '\'' +
                ", category='" + category + '\'' +
                ", desc_arr='" + desc_arr + '\'' +
                ", imUrl='" + imUrl + '\'' +
                '}';
    }

    public Apps(int appId, String asIn, String brand, String category, String desc_arr, String imUrl) {
        this.appId = appId;
        this.asIn = asIn;
        this.brand = brand;
        this.category = category;
        this.desc_arr = desc_arr;
        this.imUrl = imUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(appId);
        dest.writeString(asIn);
        dest.writeString(brand);
        dest.writeString(category);
        dest.writeString(desc_arr);
        dest.writeString(imUrl);

    }

    public Apps(Parcel in) {
        appId = in.readInt();
        asIn = in.readString();
        brand = in.readString();
        category = in.readString();
        desc_arr = in.readString();
        imUrl = in.readString();
    }

    public static final Parcelable.Creator<Apps> CREATOR = new Parcelable.Creator<Apps>()
    {
        public Apps createFromParcel(Parcel in)
        {
            return new Apps(in);
        }
        public Apps[] newArray(int size)
        {
            return new Apps[size];
        }
    };
}
