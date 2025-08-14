package com.slanf.application.views.users;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.slanf.application.data.User;
import com.slanf.application.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;

@Route("users")
public class UserView extends VerticalLayout {

    private final UserService userService;
    private Grid<User> grid = new Grid<>(User.class, false);
    private Binder<User> binder = new Binder<>(User.class);

    private TextField username = new TextField("Username");
    private TextField email = new TextField("Email");
    private PasswordField password = new PasswordField("New Password");
    private PasswordField confirmPassword = new PasswordField("Confirm Password");

    private Button saveButton = new Button("Save");
    private Button cancelButton = new Button("Cancel");

    private User currentUser;

    @Autowired
    public UserView(UserService userService) {
        this.userService = userService;

        // Configurar Grid
        grid.addColumn(User::getId).setHeader("ID").setWidth("70px").setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username");
        grid.addColumn(User::getEmail).setHeader("Email");

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                editUser(event.getValue());
            } else {
                clearForm();
            }
        });

        add(grid);

        // Configurar formulario
        binder.forField(username)
                .asRequired("Username is required")
                .bind(User::getUsername, User::setUsername);

        binder.forField(email)
                .asRequired("Email is required")
                .bind(User::getEmail, User::setEmail);

        // Password y confirmación no mapeados al binder directamente
        password.setPlaceholder("Leave empty to keep current password");
        confirmPassword.setPlaceholder("Confirm new password");

        saveButton.addClickListener(e -> saveUser());
        cancelButton.addClickListener(e -> clearForm());

        add(username, email, password, confirmPassword, saveButton, cancelButton);

        updateGrid();
    }

    private void editUser(User user) {
        currentUser = user;
        binder.readBean(user);
        password.clear();
        confirmPassword.clear();
    }

    private void clearForm() {
        currentUser = null;
        binder.readBean(new User());
        password.clear();
        confirmPassword.clear();
        grid.asSingleSelect().clear();
    }

    private void saveUser() {
        if (currentUser == null) {
            currentUser = new User();
        }

        try {
            binder.writeBean(currentUser);

            // Validar passwords
            String pass = password.getValue();
            String confirmPass = confirmPassword.getValue();
            if (!pass.isEmpty() || !confirmPass.isEmpty()) {
                if (!pass.equals(confirmPass)) {
                    Notification.show("Passwords do not match");
                    return;
                }
                // Aquí debes hashear la contraseña antes de guardar
                String hashed = hashPassword(pass);
                currentUser.setHashedPassword(hashed);
            }

            userService.save(currentUser);
            Notification.show("User saved");
            updateGrid();
            clearForm();

        } catch (Exception ex) {
            Notification.show("Error saving user: " + ex.getMessage());
        }
    }

    private void updateGrid() {
        grid.setItems(userService.findAll());
    }

    private String hashPassword(String password) {
        // Implementa aquí tu método de hash real
        // Ejemplo simple (NO usar en producción):
        return Integer.toHexString(password.hashCode());
    }
}

