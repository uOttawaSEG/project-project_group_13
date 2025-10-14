package com.example.otams.data;

import com.example.otams.data.model.LoggedInUser;
import java.util.concurrent.CompletableFuture;

/**
 * Repository that handles authentication and keeps an in-memory user cache.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;
    private final LoginDataSource dataSource;

    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
    }

    /**
     * Performs Firebase login asynchronously.
     */
    public CompletableFuture<Result<LoggedInUser>> login(String username, String password) {
        CompletableFuture<Result<LoggedInUser>> future = new CompletableFuture<>();

        dataSource.login(username, password)
                .thenAccept(result -> {
                    if (result instanceof Result.Success) {
                        LoggedInUser loggedInUser = ((Result.Success<LoggedInUser>) result).getData();
                        setLoggedInUser(loggedInUser);
                    }
                    future.complete(result);
                });

        return future;
    }
}
