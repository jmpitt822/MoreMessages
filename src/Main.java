import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.post;

/**
 * Created by jeremypitt on 9/20/16.
 */
public class Main {
    static User user;
    static Message message;
    static ArrayList<Message> messageList = new ArrayList<>();
    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) {
        User defaultUser = new User("Jeremy Pitt", "password");
        users.put(defaultUser.name, defaultUser);
        Spark.init();

//        Spark.get("/",
//                ((request, response) -> {
//                    HashMap m = new HashMap();
//                    if (user == null) {
//                        return new ModelAndView(m, "index.html");
//                    } else {
//
//                        m.put("name", user.name);
//                        m.put("messageList", messageList);
//                        return new ModelAndView(m, "messages.html");
//                    }
//                }),
//                new MustacheTemplateEngine()
//        );

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);

                    HashMap m = new HashMap();
                    if (user == null){
                        return new ModelAndView(m, "index.html");
                    }else{
                        return new ModelAndView(user, "messages.html");
                    }
                }),
                new MustacheTemplateEngine()
        );

        post(
                "create-user",
                ((request, response) -> {
                    String name = request.queryParams("name");
                    String passwordInput = request.queryParams("password");
                    if (users.containsKey(name)){
                        if (passwordInput.equals(users.get(name).password)){
                            user = new User(name, passwordInput);
                            Session session = request.session();
                            session.attribute("userName", name);
                            response.redirect("/");
                        }
                        else{
                            response.redirect("/");
                        }

                    }
                    else {
                        user = new User(name, passwordInput);
                        users.put(name, user);
                        Session session = request.session();
                        session.attribute("userName", name);
                        response.redirect("/");
                    }
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/create-message",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    if (user == null){
                        throw new Exception("User is not logged in.");
                    }

                    String newMessage = request.queryParams("message");
                    Message message = new Message(newMessage);


                    user.messages.add(message);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete-message",
                ((request, response) -> {
//                    Session session = request.session();
//                    String name = session.attribute("userName");
                    int deleteMessage = Integer.parseInt(request.queryParams("deleteMessage"));
                    user.messages.remove(deleteMessage-1);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/edit-message",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    int editNum = Integer.parseInt(request.queryParams("editNum"));
                    String newMessage = request.queryParams("editMessage");
                    user.messages.set(editNum-1, new Message(newMessage));
                    response.redirect("/");
                    return "";

                })
        );
    }
}
