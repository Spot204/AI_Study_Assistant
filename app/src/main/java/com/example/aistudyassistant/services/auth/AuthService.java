package com.example.aistudyassistant.services.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {

    private FirebaseAuth auth;

    public AuthService() {
        auth = FirebaseAuth.getInstance();
    }

    public void Login(
            String email,
            String password,
            AuthCallback callback
    ){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user =
                                auth.getCurrentUser();

                        callback.onSuccess(
                                user.getUid()
                        );

                    } else {

                        callback.onFailure(
                                task.getException().getMessage()
                        );
                    }
                });
    }

    public void Register(
            String email,
            String password,
            AuthCallback callback
    ){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user =
                                auth.getCurrentUser();

                        callback.onSuccess(
                                user.getUid()
                        );

                    } else {

                        callback.onFailure(
                                task.getException().getMessage()
                        );
                    }
                });
    }

    public void Logout(){
        auth.signOut();
    }
}
