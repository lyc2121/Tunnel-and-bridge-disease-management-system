package com.example.android.BTBLE905;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class map {
        private final double PI = 3.14159265358979323; //圆周率
        private final double R = 6371229;              //地球的半径
    Double perimeter =  2*Math.PI*R;

    //一米对应的经度（东西方向）1M实际度

        public double getLongt(double longt1, double lat1, double distance){
            Double perimeter_lontitude =   perimeter*Math.cos(Math.PI * lat1 / 180);
            double longitude_per_mi = 360 / perimeter_lontitude ;
            double a =distance*longitude_per_mi;
            return a;
        }
        public double getLat(double longt1, double lat1, double distance){
            double latitude_per_mi = 360 /perimeter;
            double a = distance*latitude_per_mi;
            return a;
        }
}
