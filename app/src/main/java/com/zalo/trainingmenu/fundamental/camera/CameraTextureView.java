package com.zalo.trainingmenu.fundamental.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_ANTIBANDING_MODE_50HZ;
import static android.hardware.camera2.CaptureRequest.CONTROL_AE_ANTIBANDING_MODE;

public class CameraTextureView extends TextureView {
    CameraDevice mCameraDevice;
    Size mPreviewSize;
    String mCameraID = "";


    public CameraTextureView(Context context) {
        super(context);
        init(null);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CameraTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

    }

    public void activeCamera() {
        if (!isAvailable()) setSurfaceTextureListener(mSurfaceTextureListener);
        else {
            setupCamera(getWidth(), getHeight());
            openCamera();
        }
    }


    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            // khi mở Camera thành công thì gán vào biến "cameraDevice"
            // về sau chung ta chỉ sử dụng biến "cameraDevie" để mở các kết nối, thực hiện các thao tác khác
            mCameraDevice = camera;
            Toasty.success(getContext(), "Camera open").show();
            createCameraPreviewSession();
        }

        // Nếu mở ko thành công thì các bạn nhớ "close" nó lại nhá
        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    public void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager != null) {
            try {
                for (String id : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                    // ở đây mình sử dụng Camera sau để thực hiện bài test.
                    if (((Integer)CameraCharacteristics.LENS_FACING_FRONT).equals(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING))) {
                        continue;
                    }

                    StreamConfigurationMap map =
                            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    // Set Size để hiển thị lên màn hình
                    mPreviewSize = getPreferredPreviewsSize(
                            map.getOutputSizes(SurfaceTexture.class), width, height);
                    mCameraID = id;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Size getPreferredPreviewsSize(Size[] mapSize, int width, int height) {
        List<Size> collectorSize = new ArrayList<>();
        for (Size option : mapSize) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    collectorSize.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    collectorSize.add(option);
                }
            }
        }
        if (collectorSize.size() > 0) {
            return Collections.min(collectorSize, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                }
            });
        }
        return mapSize[0];
    }

    public void openCamera() {
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        if(cameraManager!=null) {
            try {
                // mở kết nối tới Camera của thiết bị
                // các hành động trả về sẽ dc thực hiện trong "cameraDeviceStateCallback"
                // tham số thứ 3 của hàm openCamera là 1 "Handler"
                // nhưng hiện tại ở đây chúng ta chưa cần thiết nên mình để nó là "null"
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraManager.openCamera(mCameraID, mCameraCallback, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Khởi tạo hàm để hiển thị hình ảnh thu về từ Camera lên TextureView
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = getSurfaceTexture();
            if(surfaceTexture==null) return;
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            // Khởi tạo CaptureRequestBuilder từ cameraDevice với template truyền vào là
            // "CameraDevice.TEMPLATE_PREVIEW"
            // Với template này thì CaptureRequestBuilder chỉ thực hiện View mà thôi
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mPreviewCaptureRequestBuilder.set(CONTROL_AE_ANTIBANDING_MODE, CONTROL_AE_ANTIBANDING_MODE_50HZ);

            // Thêm đích đến cho dữ liệu lấy về từ Camera
            // Đích đến này phải nằm trong danh sách các đích đến của dữ liệu
            // được định nghĩa trong cameraDevice.createCaptureSession() "phần định nghĩa này ngay bên dưới"
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);

            // Khởi tạo 1 CaptureSession
            // Arrays.asList(previewSurface) List danh sách các Surface
            // ( đích đến của hình ảnh thu về từ Camera)
            // Ở đây đơn giản là chỉ hiển thị hình ảnh thu về từ Camera nên chúng ta chỉ có 1 đối số.
            // Nếu bạn muốn chụp ảnh hay quay video vvv thì
            // ta có thể truyền thêm các danh sách đối số vào đây
            // Vd: Arrays.asList(previewSurface, imageReader)
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    // Hàm Callback trả về kết quả khi khởi tạo.
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if (mCameraDevice == null) {
                                return;
                            }
                            try {
                                // Khởi tạo CaptureRequest từ CaptureRequestBuilder
                                // với các thông số đã được thêm ở trên
                                mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                                mCameraCaptureSession = session;
                                mCameraCaptureSession.setRepeatingRequest(
                                        mPreviewCaptureRequest,
                                        mCameraSessionCaptureCallback,
                                        null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toasty.error(getContext(),"Create camera session fail").show();
                        }
                    },
                    // Đối số thứ 3 của hàm là 1 Handler,
                    // nhưng do hiện tại chúng ta chưa làm gì nhiều nên mình tạm thời để là null
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;

    // Callback này dc sử dụng trong createCameraPreviewSession().
// Do hàm này chỉ đơn giản là hiển thị hình ảnh thu về từ Camera
// và chưa xử lý dữ liệu thu dc từ Camera nên các bạn cứ để mặc định như này là ok
    private CameraCaptureSession.CaptureCallback mCameraSessionCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                             long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session,
                                            CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                }
            };

    // Close camera khi bạn chuyển sang Activity khác hoặc khi bạn thoát khỏi ứng dụng.
    public void closeCamera(){
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
    }
}
