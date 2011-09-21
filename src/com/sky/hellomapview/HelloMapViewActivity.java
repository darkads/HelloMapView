package com.sky.hellomapview;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressWarnings("unused")
public class HelloMapViewActivity extends MapActivity {

	LinearLayout linearLayout;
	MapView mapView;

	Facebook facebook = new Facebook("222592464462347");

	public static final String FILENAME = "MyAndroid_data";
	private SharedPreferences mPrefs;
	private MapController mapController;
	private LocationManager locationManager;
	private LocationListener locationListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new GPSLocationListener();
		
		locationManager.requestLocationUpdates(
		LocationManager.GPS_PROVIDER, 
		0, 
		0, 
		locationListener);
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		mapController = mapView.getController();
		mapController.setZoom(16);

		/*
		 * Get existing access_token if any
		 */
		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		/*
		 * Only call authorize if the access_token has expired.
		 */
		if (!facebook.isSessionValid()) {
			facebook.authorize(this, new DialogListener() {
				//@Override
				public void onComplete(Bundle values) {
					SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                }

				//@Override
				public void onFacebookError(FacebookError error) {
				}

				//@Override
				public void onError(DialogError e) {
				}

				//@Override
				public void onCancel() {
				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}	
	
	 private class GPSLocationListener implements LocationListener 
	       {
	           //@Override
	           public void onLocationChanged(Location location) {
	               if (location != null) {
	                   GeoPoint point = new GeoPoint(
	                           (int) (location.getLatitude() * 1E6), 
	                           (int) (location.getLongitude() * 1E6));
	                   
	                   /* Toast.makeText(getBaseContext(), 
	                           "Latitude: " + location.getLatitude() + 
	                           " Longitude: " + location.getLongitude(), 
	                           Toast.LENGTH_SHORT).show();*/
	                                   
	                   mapController.animateTo(point);
	                   mapController.setZoom(16);
	                   
	                   // add marker
	                   MapOverlay mapOverlay = new MapOverlay();
	                   mapOverlay.setPointToDraw(point);
	                   List<Overlay> listOfOverlays = mapView.getOverlays();
	                   listOfOverlays.clear();
	                   listOfOverlays.add(mapOverlay);
	                   
	                   String address = ConvertPointToLocation(point);
	                   Toast.makeText(getBaseContext(), address, Toast.LENGTH_SHORT).show();
	   
	                   mapView.invalidate();
	              }
	          }
	          
	          public String ConvertPointToLocation(GeoPoint point) {   
	              String address = "";
	              Geocoder geoCoder = new Geocoder(
	                      getBaseContext(), Locale.getDefault());
	              try {
	                  List<Address> addresses = geoCoder.getFromLocation(
	                      point.getLatitudeE6()  / 1E6, 
	                      point.getLongitudeE6() / 1E6, 1);
	       
	                  if (addresses.size() > 0) {
	                      for (int index = 0; index < addresses.get(0).getMaxAddressLineIndex(); index++)
	                          address += addresses.get(0).getAddressLine(index) + " ";
	                  }
	              }
	              catch (IOException e) {                
	                  e.printStackTrace();
	              }   
	              
	              return address;
	          } 
	  
	          //@Override
	          public void onProviderDisabled(String provider) {
	          }
	  
	          //@Override
	          public void onProviderEnabled(String provider) {
	          }
	  
	          //@Override
	          public void onStatusChanged(String provider, int status, Bundle extras) {
	          }
	      }
	      
	      class MapOverlay extends Overlay
	      {
	          private GeoPoint pointToDraw;
	  
	          public void setPointToDraw(GeoPoint point) {
	              pointToDraw = point;
	          }
	  
	          public GeoPoint getPointToDraw() {
	              return pointToDraw;
	          }
	          
	          @Override
	          public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	              super.draw(canvas, mapView, shadow);                   
	   
	              // convert point to pixels
	              Point screenPts = new Point();
	              mapView.getProjection().toPixels(pointToDraw, screenPts);
	   
	              // add marker
	              Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
	              canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null); // 24 is the height of image        
	              return true;
	          }
	      } 
}
