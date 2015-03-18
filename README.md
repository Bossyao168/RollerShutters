#RollerShutters
类似卷帘的view显示与隐藏控件，操作类似下拉刷新。

#ScreenShot
<a href="http://s1060.photobucket.com/user/bossyao168/media/2015-03-18%2017_55_58_zpsbnuc3lmz.gif.html" target="_blank"><img src="http://i1060.photobucket.com/albums/t444/bossyao168/2015-03-18%2017_55_58_zpsbnuc3lmz.gif" border="0" alt="RollerShutter photo 2015-03-18 17_55_58_zpsbnuc3lmz.gif"/></a>

#Usage
目前使用比较简单，只需要将外部和内部的view，添加到ContentView的布局下作为子view。
然后外部再放RollerShuttersView即可

如本demo中

	```xml
     <com.bossyao.rollershutter.library.RollerShuttersView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:id="@+id/rollerShuttersView">

        <com.bossyao.rollershutter.library.ContentView
                android:id="@+id/ContentView"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

            <LinearLayout android:layout_width="match_parent"
                          android:layout_height="120dp"
                          android:background="@android:color/holo_blue_bright"
                          android:orientation="vertical"
                    >

                <Button android:layout_width="200dp"
                        android:layout_height="50dp"
                        android:text="HI"/>
            </LinearLayout>

            <LinearLayout

                    android:background="@android:color/white"
                    android:id="@+id/list_view"
                    android:layout_width="match_parent"
                    android:layout_height="50000dp"
                    android:orientation="vertical">

                <Button android:layout_width="200dp"
                        android:layout_height="50dp"
                        android:text="1"/>

                <Button android:layout_width="200dp"
                        android:layout_height="50dp"
                        android:text="2"/>

            </LinearLayout>


        </com.bossyao.rollershutter.library.ContentView>
    </com.bossyao.rollershutter.library.RollerShuttersView>
    ```

# Last
这是我第一个上传到github的东西，希望大家喜欢，当然这个是简单了点，后续我会慢慢扩展，将更好的控件或别的东西分享。
