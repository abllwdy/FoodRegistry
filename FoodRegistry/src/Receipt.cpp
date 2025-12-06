#include "Receipt.h"
#include <iostream>
#include <iomanip>
using namespace std;

void Receipt::printSeparator(int length) {
    cout << setw(length) << setfill('=') << "" << endl;
}

void Receipt::printSeparatorToFile(int length) {
    outFile << setw(length) << setfill('=') << "" << endl;
}

void Receipt::printHeader() {
    string header = "= * === * === * === * === * === * ==";
    string title = "H & S Restaurant Meal Billing System";
    cout << header << endl << title << endl << header << endl << endl;
}

void Receipt::printHeaderToFile() {
    string header = "= * === * === * === * === * ==";
    string title = "H & S Restaurant Meal Billing System";
    outFile << header << endl << title << endl << header << endl << endl;
}

void Receipt::printCheckout(const Order& order, float amountPaid, int orderNumber) {
    float change = amountPaid - order.getTotal();

    printSeparator(36);
    cout << " Subtotal: RM " << fixed << setprecision(2) << order.getSubtotal() << endl;
    cout << " SST charge 10%: RM " << order.getSST() << endl;
    cout << " Grand total: RM " << order.getTotal() << endl;
    printSeparator(36);
    cout << "Amount received: RM " << amountPaid << endl;
    if (change > 0) {
        cout << " Change given: RM " << change << endl;
    }
    printSeparator(36);
    cout << "Thank you and have a nice day ahead!" << endl;
}

void Receipt::saveToFile(const Order& order, const vector<MenuItem>& menu, 
                        float amountPaid, int orderNumber) {
    outFile.open("ReceiptNo" + to_string(orderNumber) + ".txt");
    if (!outFile.is_open()) return;

    printHeaderToFile();
    outFile << "Order: #" << orderNumber << endl << endl;
    outFile << "Items in bill:" << endl;

    for (size_t i = 0; i < menu.size(); i++) {
        if (order.getQuantity(i) > 0) {
            outFile << "(" << menu[i].getCode() << ") [RM " 
                   << fixed << setprecision(2) << menu[i].getPrice() << "] "
                   << menu[i].getName() << " × " << order.getQuantity(i)
                   << " = RM " << menu[i].getPrice() * order.getQuantity(i) << endl;
        }
    }

    float change = amountPaid - order.getTotal();
    printSeparatorToFile(36);
    outFile << " Subtotal: RM " << fixed << setprecision(2) << order.getSubtotal() << endl;
    outFile << " SST charge 10%: RM " << order.getSST() << endl;
    outFile << " Grand total: RM " << order.getTotal() << endl;
    printSeparatorToFile(36);
    outFile << "Amount received: RM " << amountPaid << endl;
    if (change > 0) {
        outFile << " Change given: RM " << change << endl;
    }
    printSeparatorToFile(36);
    outFile << "Thank you and have a nice day ahead!" << endl;

    outFile.close();
}
