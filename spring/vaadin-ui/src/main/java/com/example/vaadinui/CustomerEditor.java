package com.example.vaadinui;

import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;

@SpringComponent
@UIScope
@Log
public class CustomerEditor extends VerticalLayout {

    private final CustomerRepository repository;

    private Customer customer;

    private TextField firstName = new TextField("First name");

    private Button save = new Button("Save", FontAwesome.SAVE);
    private Button cancel = new Button("Cancel");
    private Button delete = new Button("Delete", FontAwesome.TRASH_O);
    private TextField lastName = new TextField("Last name");
    private CssLayout actions = new CssLayout(save, cancel, delete);

    private Binder<Customer> binder = new Binder<>(Customer.class);

    @Autowired
    public CustomerEditor(CustomerRepository repository) {
        this.repository = repository;

        addComponents(firstName, lastName, actions);

        binder.bindInstanceFields(this);

        setSpacing(true);
        actions.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        save.addClickListener(e -> repository.save(customer));
        delete.addClickListener(e -> repository.save(customer));
        cancel.addClickListener(e -> editCustomer(customer));
        setVisible(false);
    }

    public interface ChangeHandler {

        void onChange();
    }

    public void editCustomer(Customer c) {
        if (c == null) {
            setVisible(false);
            return;
        }
        log.info("Customer is: " + c);
        final boolean persisted = c.getId() != null;

        if (persisted) {
            customer = repository.findOne(c.getId());
        } else {
            customer = c;
        }
        cancel.setVisible(persisted);

        binder.setBean(customer);

        setVisible(true);

        // a hack to ensure it's visible... a hack?
        save.focus();
        firstName.selectAll();
    }

    public void setChangeHandler(ChangeHandler h) {
        save.addClickListener(e -> h.onChange());
        delete.addClickListener(e -> h.onChange());
    }
}
