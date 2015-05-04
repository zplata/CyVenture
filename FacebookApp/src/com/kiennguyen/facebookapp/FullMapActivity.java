package com.kiennguyen.facebookapp;


import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.homescreen.util.SystemUiHider;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("NewApi")
public class FullMapActivity extends FragmentActivity implements 
	View.OnClickListener, GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private Button mapBackButton;
	private ImageButton mapNextItem;
	private ImageButton mapPrevItem;
	private TextView gameName;
	private ListView curPlayersList;
	private TextView userView;
	
	private String username;
	private String mapGameName;
	private ArrayList<String> usersList;
	LocationClient mLocationClient;
	GoogleMap mMap;
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_map);
        if (initMap()) {
        	//Toast.makeText(this , "Ready to google map!", Toast.LENGTH_SHORT).show();
        	//mMap.setMyLocationEnabled(true);
        	mLocationClient = new LocationClient(this, this, this);
        	mLocationClient.connect();
        	gotoLocation(42.02601, -93.64836, 16);
        }
        else {
        	Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
        }
        
    }
	 private void gotoLocation(double lat, double lng,
				float zoom) {
	    	LatLng ll = new LatLng(lat, lng);
	    	CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
	    	mMap.moveCamera(update);
		}
	

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_full_map, container, false);
		super.onCreate(savedInstanceState);
		
		//Map Screen is full-screen, not ActionBar needed
		ActionBar bar = getActionBar();
		bar.hide();
		
		//****Getters of all Views****
		
		//replace this one with Kien's layout
		userView = (TextView) rootView.findViewById(R.layout.activity_view_games_screen);
		
		curPlayersList = (ListView) rootView.findViewById(R.id.curPlayersList);
		gameName = (TextView) rootView.findViewById(R.id.mapGameName);
		mapBackButton = (Button) rootView.findViewById(R.id.mapBackButton);
		mapNextItem = (ImageButton) rootView.findViewById(R.id.mapNextItem);
		mapPrevItem = (ImageButton) rootView.findViewById(R.id.mapPrevItem);
		
		//*****Get the game name******
		Firebase fbGameName = new Firebase("https://clues309.firebaseIO.com/users/" + username + "/games");
		fbGameName.addValueEventListener(new ValueEventListener(){

			@Override
			public void onCancelled(FirebaseError arg0) {
				System.out.println("ValueEventListener cancelled");
			}

			@Override
			public void onDataChange(DataSnapshot snap) {
				//NEED to take care of multiple games case
				mapGameName = (String) snap.getValue();
			}
			
		});
		
		gameName.setText(mapGameName);
			
		
		//*****Users will be a ListView*****
		Firebase fbUsers = new Firebase("https://clues309.firebaseIO.com/usersGames/" + mapGameName);
		fbUsers.addValueEventListener(new ValueEventListener(){

			@Override
			public void onCancelled(FirebaseError arg0) {
				System.out.println("ValueEventListener cancelled");
				
			}

			@Override
			public void onDataChange(DataSnapshot snap) {
				Object value = snap.getValue();
				if(value == null){
					System.out.println("No users found");
				}
				else{
					//For each child under a game in usersGames, add to usersList
					for (DataSnapshot child: snap.getChildren()){
						usersList.add((String) child.getValue());
					}
				}
			}
			
		});
			
		
		//**SHOULD REPLACE R.layout.view_game with R.layout.list_layout_file
		//ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.view_game, usersList);
		ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(this, R.layout.activity_view_games_screen, usersList);
		curPlayersList.setAdapter(userAdapter);
		
		
		
		//*****Color marking icons map to each user*******
		//for(View v: curPlayersList.getViews){
		//	v.setText.fontColor(//RANDOM COLOR);
		//}
		
		
		//*****Insert map into LinearLayout through fragment********
		
		
		
		
		Intent i = getIntent();
		return rootView;
		
	}
	
	private boolean initMap() {
    	if (mMap == null) {
    		MapFragment mapFrag = 
    				(MapFragment) getFragmentManager().findFragmentById(R.id.map);
    		mMap = mapFrag.getMap();
    	}
    	return (mMap != null);
    }
	
	@Override
	public void onClick(View v) {
		mapBackButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//PUT HOMESCREEN.CLASS IN HERE!!
				Intent goHome = new Intent(getApplicationContext(), Home.class);
				startActivity(goHome);
			}
		});
		
		mapNextItem.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//GO TO NEXT VIEW GAME MAP
				Intent stayHere = new Intent(getApplicationContext(), FullMapActivity.class);
				startActivity(stayHere);
				
				//*****Handle other games pulling from the Firebase Database (left and right map toggle)*******
				Firebase fbGamesToggle = new Firebase("https://clues309.firebaseIO.com/users/" + username + "/games");
				fbGamesToggle.addValueEventListener(new ValueEventListener(){

					@Override
					public void onCancelled(FirebaseError arg0) {
						// TODO Auto-generated method stub
						System.out.println("ValueEventListener Cancelled");
					}

					@Override
					public void onDataChange(DataSnapshot snap) {
						// TODO Auto-generated method stub
						String userGamesParser = (String) snap.getValue();
						if(userGamesParser != null){
							String build = "";
							for(int i = 0; i < userGamesParser.length(); i++){
								if(userGamesParser.charAt(i) == ',' || userGamesParser.charAt(i) == '}'){
									usersList.add(build);
								}
								else if(Character.isDigit(userGamesParser.charAt(i))){
									build = build + userGamesParser.charAt(i);
								}
								else{
									//Do nothing
								}
							}
							for(int i = 0; i < usersList.size(); i++){
								//Iterates through all game names in this list, toggles map
								//Swipe through all CURRENT games going FORWARDS
								//**Stretch goal
							}
						}
						
					}
					
				});
			}
		});
		
		mapPrevItem.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//GO TO PREV VIEW GAME MAP
				Intent stayHere = new Intent(getApplicationContext(), FullMapActivity.class);
				startActivity(stayHere);
				
				//*****Handle other games pulling from the Firebase Database (left and right map toggle)*******
				Firebase fbGamesToggle = new Firebase("https://clues309.firebaseIO.com/users/" + username + "/games");
				fbGamesToggle.addValueEventListener(new ValueEventListener(){

					@Override
					public void onCancelled(FirebaseError arg0) {
						// TODO Auto-generated method stub
						System.out.println("ValueEventListener Cancelled");
					}

					@Override
					public void onDataChange(DataSnapshot snap) {
						// TODO Auto-generated method stub
						String userGamesParser = (String) snap.getValue();
						if(userGamesParser != null){
							String build = "";
							for(int i = 0; i < userGamesParser.length(); i++){
								if(userGamesParser.charAt(i) == ',' || userGamesParser.charAt(i) == '}'){
									usersList.add(build);
								}
								else if(Character.isDigit(userGamesParser.charAt(i))){
									build = build + userGamesParser.charAt(i);
								}
								else{
									//Do nothing
								}
							}
							for(int i = 0; i < usersList.size(); i++){
								//Iterates through all game names in this list, toggles map
								//Swipe through all CURRENT games going BACKWARDS
								//**Stretch goal
							}
						}
						
					}
					
				});
			}
		});
	
		
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
}
