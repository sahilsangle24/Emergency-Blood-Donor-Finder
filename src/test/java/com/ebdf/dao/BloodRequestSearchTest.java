package com.ebdf.dao;
/** Manual smoke test. Supply a registered donor USER_ID. */
public class BloodRequestSearchTest {
 public static void main(String[] args) {
  if(args.length==0){System.out.println("Usage: BloodRequestSearchTest <donorUserId>");return;}
  try { System.out.println(new BloodRequestSearchDAO().getOpenBloodRequests(Integer.parseInt(args[0]))); }
  catch(Exception e){e.printStackTrace();}
 }
}
