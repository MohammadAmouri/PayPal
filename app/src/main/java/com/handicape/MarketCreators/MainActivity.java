package com.handicape.MarketCreators;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalOAuthScopes;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ShippingAddress;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Button;
import java.math.BigDecimal;


public class MainActivity extends AppCompatActivity {



    private static final String CONFIGERED_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
    // Below the user code
    private static final String CONFIGERD_CLIENT_ID = "AQp4ChFxJp6vOKUFe-JluPeF3WyP7hszZD4ZwF7BSSklmKuElGCGtlVnlIJnJeSgNUYjEzp_arbE29E2";
    private static final int REQUEST_PAYMENT = 1;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIGERED_ENVIRONMENT)
            .clientId(CONFIGERD_CLIENT_ID);
    Button payPalAction;

    // This value to adjust the donated amount
    public double DONATE_VALUE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Configure attributes for Paypal Activity
        Intent intentExtraPaypal = new Intent(this, PayPalService.class);
        intentExtraPaypal.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intentExtraPaypal);
        payPalAction = (Button) findViewById(R.id.donatebtn);
        final RadioGroup amountRadioGroup = (RadioGroup) findViewById(R.id.amountGroup);



        // Adjust the amount and open the Paypal activity
        payPalAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectedRadioBtnID = amountRadioGroup.getCheckedRadioButtonId();
                switch(selectedRadioBtnID){
                    case R.id.oneDollar:
                        DONATE_VALUE = 1;
                        onPaymentPressed();
                        break;

                    case R.id.tenDollars:
                        DONATE_VALUE = 10;
                        onPaymentPressed();
                        break;

                    case R.id.fiftyDollars:
                        DONATE_VALUE =50;
                        onPaymentPressed();
                        break;

                    case R.id.hundredDollars:
                        DONATE_VALUE =100;
                        onPaymentPressed();
                        break;

                    default:
                        Toast.makeText(MainActivity.this, "يرجى إختيار المبلغ", Toast.LENGTH_SHORT).show();
                        break;

                }

            }
        });
    }



    // Paypal activity configurations
    public void onPaymentPressed() {
        PayPalPayment amountToPay = getPay(PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent_pay = new Intent(MainActivity.this, PaymentActivity.class);
        intent_pay.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent_pay.putExtra(PaymentActivity.EXTRA_PAYMENT, amountToPay);
        startActivityForResult(intent_pay, REQUEST_PAYMENT);
    }


    // Data sent to Paypal
    private PayPalPayment getPay(String paymentIntent) {


        return new PayPalPayment(new BigDecimal(DONATE_VALUE), "USD", "sample item",
                paymentIntent);
    }



    // For retrieve the messages after process
    @Override
    protected void onActivityResult(int req_Code, int res_Code, Intent ret_data) {
        super.onActivityResult(req_Code, res_Code, ret_data);
        if (req_Code == REQUEST_PAYMENT) {
            if (res_Code == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        ret_data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.e("show", confirm.toJSONObject().toString(4));
                        Log.e("show", confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         */
                        Toast.makeText(getApplicationContext(), "تم تأكيد الدفعة من " +
                                " PayPal ", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "خطأ" ,Toast.LENGTH_LONG).show();
                    }
                }
            } else if (res_Code == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "تم إلغاء العملية", Toast.LENGTH_LONG).show();
            } else if (res_Code == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(getApplicationContext(), "دفعة خاطئة / خطأ في النظام" , Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();



    }}
