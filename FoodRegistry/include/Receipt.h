#ifndef RECEIPT_H
#define RECEIPT_H

#include <fstream>
#include <vector>
#include "Order.h"
#include "MenuItem.h"
using namespace std;

class Receipt {
private:
    ofstream outFile;

    void printSeparator(int length);
    void printSeparatorToFile(int length);
    void printHeader();
    void printHeaderToFile();

public:
    void printCheckout(const Order& order, float amountPaid, int orderNumber);
    void saveToFile(const Order& order, const vector<MenuItem>& menu, 
                    float amountPaid, int orderNumber);
};

#endif
