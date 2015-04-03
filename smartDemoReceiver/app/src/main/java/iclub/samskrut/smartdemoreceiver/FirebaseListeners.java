package iclub.samskrut.smartdemoreceiver;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class FirebaseListeners {
    static Firebase ref;
    static ChildEventListener pidChildEventListener;
    static ChildEventListener vidChildEventListener;
    static ChildEventListener three60ChildEventListener;
    static ValueEventListener tabValueEventListener;
    static ValueEventListener ssValueEventListener;
}