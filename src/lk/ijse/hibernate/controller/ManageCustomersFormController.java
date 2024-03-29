package lk.ijse.hibernate.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.hibernate.entity.Customer;
import lk.ijse.hibernate.repositry.CustomerRepository;
import lk.ijse.hibernate.view.tdm.CustomerTM;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;


public class ManageCustomersFormController {
    public AnchorPane root;
    public JFXTextField txtCustomerName;
    public JFXTextField txtCustomerId;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    public JFXTextField txtCustomerAddress;
    public TableView<CustomerTM> tblCustomers;
    public JFXButton btnAddNewCustomer;


    CustomerRepository customerRepository;

    public void initialize() {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        initUI();

        tblCustomers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDelete.setDisable(newValue == null);
            btnSave.setText(newValue != null ? "Update" : "Save");
            btnSave.setDisable(newValue == null);

            if (newValue != null) {
                Customer customer=new Customer();
                txtCustomerId.setText(String.valueOf(tblCustomers.getSelectionModel().getSelectedItem().getId()));
                txtCustomerName.setText(newValue.getName());
                txtCustomerAddress.setText(newValue.getAddress());

                txtCustomerId.setDisable(false);
                txtCustomerName.setDisable(false);
                txtCustomerAddress.setDisable(false);
            }
        });

        txtCustomerAddress.setOnAction(event -> btnSave.fire());
        loadAllCustomers();
    }

    private void loadAllCustomers() {
        customerRepository=new CustomerRepository();
        tblCustomers.getItems().clear();
        /*Get all customers*/
        ArrayList<Customer> arrayList= customerRepository.allCustomers();
        /*for (Object array:arrayList) {
            tblCustomers.getItems().add(new CustomerTM(
                    String.valueOf(arrayList.get(1)), String.valueOf(arrayList.get(2))));
        }*/
        for (Customer customer:arrayList){
            tblCustomers.getItems().add(new CustomerTM(customer.getId(),
                    customer.getName(), customer.getAddress()));

        }
        /*while (iterator.hasNext()) {

            tblCustomers.getItems().add(new CustomerTM(arrayList.get(i).getId(),
                    arrayList.get(i).getName(), arrayList.get(i).getAddress()));
            i++;
        }*/


    }

    private void initUI() {
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtCustomerId.setDisable(true);
        txtCustomerName.setDisable(true);
        txtCustomerAddress.setDisable(true);
        txtCustomerId.setEditable(false);
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
    }

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/lk/ijse/hibernate/view/main-form.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        Platform.runLater(() -> primaryStage.sizeToScene());
    }

    public void btnAddNew_OnAction(ActionEvent actionEvent) {
        customerRepository=new CustomerRepository();
        txtCustomerId.setDisable(false);

        txtCustomerName.setDisable(false);
        txtCustomerAddress.setDisable(false);
        txtCustomerId.clear();


        txtCustomerId.setText(String.valueOf(customerRepository.getNext()));
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtCustomerName.requestFocus();
        btnSave.setDisable(false);
        btnSave.setText("Save");
        tblCustomers.getSelectionModel().clearSelection();
    }


    public void btnSave_OnAction(ActionEvent actionEvent) {
        int id = Integer.parseInt(txtCustomerId.getText());
        String name = txtCustomerName.getText();
        String address = txtCustomerAddress.getText();

        if (!name.matches("[A-Za-z ]+")) {
            new Alert(Alert.AlertType.ERROR, "Invalid name").show();
            txtCustomerName.requestFocus();
            return;
        } else if (!address.matches(".{3,}")) {
            new Alert(Alert.AlertType.ERROR, "Address should be at least 3 characters long").show();
            txtCustomerAddress.requestFocus();
            return;
        }

        if (btnSave.getText().equalsIgnoreCase("save")) {
            /*Save Customer*/
            try {
                if (!existCustomer(id)) {
                    new Alert(Alert.AlertType.ERROR, id + " already exists").show();
                }
               /* Connection connection = DBConnection.getDbConnection().getConnection();
                PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer (id,name, address) VALUES (?,?,?)");
                pstm.setString(1, id);
                pstm.setString(2, name);
                pstm.setString(3, address);
                pstm.executeUpdate();*/
                customerRepository=new CustomerRepository();
                Customer customer=new Customer(id,name,address);
                customerRepository.saveCustomer(customer);

                tblCustomers.getItems().add(new CustomerTM(id, name, address));
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Failed to save the customer " + e.getMessage()).show();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        } else {
            /*Update customer*/

            try {
                if (!existCustomer(id)) {
                    new Alert(Alert.AlertType.ERROR, "There is no such customer associated with the id " + id).show();
                }
                /*Connection connection = DBConnection.getDbConnection().getConnection();
                PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?, address=? WHERE id=?");
                pstm.setString(1, name);
                pstm.setString(2, address);
                pstm.setString(3, id);
                pstm.executeUpdate();*/
                customerRepository=new CustomerRepository();
                Customer customer=new Customer(id,name,address);
               customerRepository.updateCustomer(customer);

            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Failed to update the customer " + id + e.getMessage()).show();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            CustomerTM selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            selectedCustomer.setName(name);
            selectedCustomer.setAddress(address);
            tblCustomers.refresh();
        }

        btnAddNewCustomer.fire();
    }


    boolean existCustomer(int id) throws SQLException, ClassNotFoundException {
        customerRepository=new CustomerRepository();
        return customerRepository.existCustomer(id);
    }


    public void btnDelete_OnAction(ActionEvent actionEvent) {
        /*Delete Customer*/
        int id = tblCustomers.getSelectionModel().getSelectedItem().getId();
        try {
            if (!existCustomer(id)) {
                new Alert(Alert.AlertType.ERROR, "There is no such customer associated with the id " + id).show();
            }
           /* Connection connection = DBConnection.getDbConnection().getConnection();
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
            pstm.setString(1, id);
            pstm.executeUpdate();*/
            customerRepository=new CustomerRepository();

            customerRepository.deleteCustomer(id);

            tblCustomers.getItems().remove(tblCustomers.getSelectionModel().getSelectedItem());
            tblCustomers.getSelectionModel().clearSelection();
            initUI();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete the customer " + id).show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

   /* private String generateNewId() {
        try {
            *//*Connection connection = DBConnection.getDbConnection().getConnection();
            ResultSet rst = connection.createStatement().executeQuery("SELECT id FROM Customer ORDER BY id DESC LIMIT 1;");
            if (rst.next()) {
                String id = rst.getString("id");
                int newCustomerId = Integer.parseInt(id.replace("C00-", "")) + 1;
                return String.format("C00-%03d", newCustomerId);*//*

            } else {
                return "C00-001";
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to generate a new id " + e.getMessage()).show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        if (tblCustomers.getItems().isEmpty()) {
            return "C00-001";
        } else {
            String id = getLastCustomerId();
            int newCustomerId = Integer.parseInt(id.replace("C", "")) + 1;
            return String.format("C00-%03d", newCustomerId);
        }

    }*/

   /* private String getLastCustomerId() {
        List<CustomerTM> tempCustomersList = new ArrayList<>(tblCustomers.getItems());
        Collections.sort(tempCustomersList);
        return tempCustomersList.get(tempCustomersList.size() - 1).getId();
    }*/


}
