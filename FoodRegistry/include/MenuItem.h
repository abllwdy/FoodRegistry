#ifndef MENUITEM_H
#define MENUITEM_H

#include <string>
using namespace std;

class MenuItem {
private:
    char code;
    string name;
    float price;

public:
    MenuItem(char c, string n, float p);

    char getCode() const;
    string getName() const;
    float getPrice() const;
    void display() const;
};

#endif
