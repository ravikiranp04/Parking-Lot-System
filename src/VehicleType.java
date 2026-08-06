public enum VehicleType {
    CAR(40),
    BIKE(20),
    BUS(80);
    private int hourlyPrice;
    VehicleType(int price){
        this.hourlyPrice=price;
    }
    int getHourlyPrice(){
        return hourlyPrice;
    }
}
