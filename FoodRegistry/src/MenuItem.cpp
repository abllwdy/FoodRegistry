#include "MenuItem.h"
#include <iostream>
#include <iomanip>
using namespace std;

MenuItem::MenuItem(char c, string n, float p) : code(c), name(n), price(p) {}

char MenuItem::getCode() const { 
    return code; 
}

string MenuItem::getName() const { 
    return name; 
}

float MenuItem::getPrice() const { 
    return price; 
}

void MenuItem::display() const {
    cout << '(' << code << ") " << name << endl;
    cout << "Price: RM " << fixed << setprecision(2) << price << endl;
}
