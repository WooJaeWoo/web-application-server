package model;

import java.util.Map;
import java.util.HashMap;

public class UserDB {
	static Map<String, User> userDB = new HashMap<String, User>();
	
	public static void addUser(User user) {
		userDB.put(user.getUserId(), user);
	}
	
	public static User getUser(String userId) {
		return userDB.get(userId);
	}
}
