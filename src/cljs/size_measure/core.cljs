(ns size-measure.core
  (:require
   [reagent.core :as r :refer [atom]]
   [re-frame.core :as rf]
   [ajax.core :as http]
   [day8.re-frame.http-fx]
   [size-measure.utils :as utils]
   ["antd" :as ant]))

(def FormItem (.-Item ant/Form))
(def InputSearch (.-Search ant/Input))

(def size-base-url "http://192.168.0.189:8080/automeasure")
(def size-body-size-list (str size-base-url "/public/admin/measure/data/r"))
(def size-standard-size-list (str size-base-url "/public/standard/size/data/record"))
(def size-standard-size-save (str size-base-url "/public/standard/size/data/entry"))

(declare data->db)

(rf/reg-sub
 :body-size
 (fn [db _]
   (-> db :measuresize :body-size)))
(rf/reg-sub
 :standard-size
 (fn [db _]
   (-> db :measuresize :standard-size)))
(rf/reg-sub
 :search
 (fn [db _] (-> db :measuresize :search)))
(rf/reg-sub
 :form-tips
 (fn [db _] (-> db :measuresize :form-tips)))
(rf/reg-sub
 :submit-message
 (fn [db _] (-> db :measuresize :submit-message)))


(def info (rf/subscribe [:body-size]))
;; (def search (rf/subscribe [:search]))

(rf/reg-event-db
 :init
 (fn [_ _]
   {:measuresize
    {:body-size nil
     :standard-size {}
     :search {:search-name ""}
     :form-tips "请先搜索用户"
     :submit-message ""}}))

(rf/dispatch-sync [:init])

(defn- concat-key [keys]
  (concat [:measuresize] keys))

(defn- data->db [db keys value]
  (assoc-in db (concat-key keys) value))

(defn- get-value-from-db [db keys]
  (get-in db (concat-key keys)))

;;存储量体数据列表内容
(rf/reg-event-fx
 ::set-size-body-size-list
 (fn [cofx [_ list]]
   (let [body-size (-> list
                       :data
                       :content
                       first)
         params  (select-keys body-size [:standardSizeId] )]
     {:db (data->db (:db cofx) [:body-size] body-size)
      :dispatch [:search-standard-size params]})))

(rf/reg-event-db
 ::set-size-standard-size
 (fn [db [_ result]]
   (let []
     (data->db db [:standard-size] (-> result :data)))))

;;提交成功后清空量体数据列表内容
(rf/reg-event-db
 ::submit-standard-size-callback
 (fn [db [_ result]]
   (if (= 0 (-> result :code))
     (data->db (data->db db [:body-size] nil ) [:submit-message] "")
     (data->db db [:submit-message] (-> result :message)))
   ))

;;添加根据姓名查询量体数据列表
(rf/reg-event-fx
 :search-body-size-list
 (fn [cofx [_ param]]
   {:http-xhrio {:uri size-body-size-list
                 :method :get
                 :params param
                 :timeout 10000
                 :format          (http/json-request-format)
                 :response-format (http/json-response-format {:keywords? true})
                 :on-success [::set-size-body-size-list]
                 :on-failure [:common/get-error]}}))

;;添加根据姓名查询量体数据列表
(rf/reg-event-fx
 :search-standard-size
 (fn [cofx [_ param]]
   {:http-xhrio {:uri size-standard-size-list
                 :method :get
                 :params param
                 :timeout 10000
                 :format          (http/json-request-format)
                 :response-format (http/json-response-format {:keywords? true})
                 :on-success [::set-size-standard-size]
                 :on-failure [:common/get-error]}}))

;;添加量体数据标准数据提交
(rf/reg-event-fx
 :submit-standard-size
 (fn [cofx [_ param]]
   {:http-xhrio {:uri size-standard-size-save
                 :method :post
                 :params param
                 :timeout 10000
                 :format          (http/json-request-format)
                 :response-format (http/json-response-format {:keywords? true})
                 :on-success [::submit-standard-size-callback]
                 :on-failure [:common/post-failure]}}))

(defn size-search []
  (let [search (r/atom {:search-name ""})]
    (fn [] [:div {:style {:margin-bottom 10}}
            [:div {:style {:text-align "center" :margin-bottom 10}}
             [:h3 [:strong "标准数据录入页面"]]]
            [:> ant/Row
             [:> ant/Col {:span 6} "姓名:"]
             [:> ant/Col {:span 18}
              [:> InputSearch
               {:placeholder "请输入姓名" :enterButton "搜索"
                :value (:search-name @search)
                :on-change #(swap! search assoc :search-name (-> % .-target .-value))
                :onSearch (fn [] (rf/dispatch [:search-body-size-list
                                               {:measureNameLike (:search-name @search)
                                                :clientId "hl190806354513392989"
                                                :page 0 :size 1}])) }]]
             ]])))

(defn size-form []
  (let [form-data (r/atom @(rf/subscribe [:standard-size]))]
    (fn []
      (if (nil? @info)
        [:div [:strong @(rf/subscribe [:form-tips])]]
        (let [;this (utils/get-form)
              ]
          [:div
           [:> ant/Row {:style {:margin-bottom 10}}
            [:> ant/Col {:span 8 :style {:margin-top "5vh"}}
             [:div [:span "用户："] [:span (:userName @info)]]
             [:div [:span "身高："] [:span (:height @info)] [:span " cm"]]
             [:div [:span "体重："] [:span (:height @info)] [:span " kg"]]
             [:div [:span "性别："] [:span (if (= "f" (:sex @info)) "女" "男")]]]
            [:> ant/Col {:span 8}
             [:img {:src (:imageName @info) :style {:height "20vh"}}]]
            [:> ant/Col {:span 8}
             [:img {:src (clojure.string/replace (:imageName @info) #"F." "S.") :style {:height "20vh"}}]]
            ]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "量体项"]]]
            [:> ant/Col {:span 6} [:span [:strong "智量数据"]]]
            [:> ant/Col {:span 12} [:span [:strong "手量录入数据"]]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "颈围"]]]
            [:> ant/Col {:span 6} [:span (:neck @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量颈围"
               :value (:neck @form-data)
               :on-change #(swap! form-data assoc :neck (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "头围"]]]
            [:> ant/Col {:span 6} [:span (:headCricle @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量头围"
               :value (:headSize @form-data)
               :on-change #(swap! form-data assoc :headSize (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "总肩宽"]]]
            [:> ant/Col {:span 6} [:span (:shoulder @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量总肩宽"
               :value (:shoulder @form-data)
               :on-change #(swap! form-data assoc :shoulder (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "前肩宽"]]]
            [:> ant/Col {:span 6} [:span (:frontShoulder @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量前肩宽"
               :value (:frontShoulder @form-data)
               :on-change #(swap! form-data assoc :frontShoulder (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "胸围"]]]
            [:> ant/Col {:span 6} [:span (:chest @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量胸围"
               :value (:chest @form-data)
               :on-change #(swap! form-data assoc :chest (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "中腰围"]]]
            [:> ant/Col {:span 6} [:span (:midWaist @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量中腰围"
               :value (:midWaist @form-data)
               :on-change #(swap! form-data assoc :midWaist (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "裤腰围"]]]
            [:> ant/Col {:span 6} [:span (:abdomen @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量裤腰围"
               :value (:abdomen @form-data)
               :on-change #(swap! form-data assoc :abdomen (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "臀围"]]]
            [:> ant/Col {:span 6} [:span (:buttock @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量臀围"
               :value (:buttock @form-data)
               :on-change #(swap! form-data assoc :buttock (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "腿根围"]]]
            [:> ant/Col {:span 6} [:span (:legEnd @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量腿根围"
               :value (:legEnd @form-data)
               :on-change #(swap! form-data assoc :legEnd (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "前腰高"]]]
            [:> ant/Col {:span 6} [:span (:frontWaist @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量前腰高"
               :value (:frontWaist @form-data)
               :on-change #(swap! form-data assoc :frontWaist (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "后腰高"]]]
            [:> ant/Col {:span 6} [:span (:backWaist @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量后腰高"
               :value (:backWaist @form-data)
               :on-change #(swap! form-data assoc :backWaist (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "前腰节长"]]]
            [:> ant/Col {:span 6} [:span (:frontWaistline @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量前腰节长"
               :value (:frontWaistline @form-data)
               :on-change #(swap! form-data assoc :frontWaistline (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "后腰节长"]]]
            [:> ant/Col {:span 6} [:span (:backWaistline @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量后腰节长"
               :value (:backWaistline @form-data)
               :on-change #(swap! form-data assoc :backWaistline (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "后衣长"]]]
            [:> ant/Col {:span 6} [:span (:backClothe @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量后衣长"
               :value (:backClothe @form-data)
               :on-change #(swap! form-data assoc :backClothe (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "通裆"]]]
            [:> ant/Col {:span 6} [:span (:crotch @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量通裆"
               :value (:crotch @form-data)
               :on-change #(swap! form-data assoc :crotch (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "左手腕围"]]]
            [:> ant/Col {:span 6} [:span (:wrist @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量左手腕围"
               :value (:leftWrist @form-data)
               :on-change #(swap! form-data assoc :leftWrist (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "右手腕围"]]]
            [:> ant/Col {:span 6} [:span (:wrist @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量右手腕围"
               :value (:rightWrist @form-data)
               :on-change #(swap! form-data assoc :rightWrist (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "上臂围"]]]
            [:> ant/Col {:span 6} [:span (:upperArm @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量上臂围"
               :value (:upperArm @form-data)
               :on-change #(swap! form-data assoc :upperArm (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "左袖长"]]]
            [:> ant/Col {:span 6} [:span (:leftArm @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量左袖长"
               :value (:leftSleeve @form-data)
               :on-change #(swap! form-data assoc :leftSleeve (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "右袖长"]]]
            [:> ant/Col {:span 6} [:span (:arm @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量右袖长"
               :value (:rightSleeve @form-data)
               :on-change #(swap! form-data assoc :rightSleeve (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 5}}
            [:> ant/Col {:span 6} [:span [:strong "左裤长"]]]
            [:> ant/Col {:span 6} [:span (:leg @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量左裤长"
               :value (:leftTrouser @form-data)
               :on-change #(swap! form-data assoc :leftTrouser (-> % .-target .-value))
               }]]]
           [:> ant/Row {:style {:margin-bottom 10}}
            [:> ant/Col {:span 6} [:span [:strong "右裤长"]]]
            [:> ant/Col {:span 6} [:span (:leg @info)]]
            [:> ant/Col {:span 12}
             [:> ant/Input
              {:style {:width "100%"} :placeholder "输入手量右裤长"
               :value (:rightTrouser @form-data)
               :on-change #(swap! form-data assoc :rightTrouser (-> % .-target .-value))
               }]]]

           [:div {:style {:margin-bottom 5 :color "red"}}
            [:span @(rf/subscribe [:submit-message])]]
           [:div {:style {:margin-bottom 5}}
            [:> ant/Row
             [:> ant/Col {:span 6}
              [:> ant/Button
               {:style {:width "100%"}
                :type "default"
                :onClick (fn [] (swap! form-data @(rf/subscribe [:standard-size])))}
               "清空"]]
             [:> ant/Col {:span 16 :offset 2}
              [:> ant/Button
               {:style {:width "100%"}
                :type "primary"
                :onClick (fn [] (rf/dispatch [:submit-standard-size
                                             (swap! form-data assoc  :userName (:userName @info)
                                                    :phoneNo (:phoneNo @info)
                                                    :bodySizeId (:bodySizeId @info)
                                                    :standardSizeId (:standardSizeId @info))]))}
               "提交"]]]]
           ])))))

(defn ^:dev/after-load mount-root []
  (r/render [:div {:style {:width "90%" :margin "auto" :margin-top 10 :margin-bottom 20}}
             [size-search]
             ;;(utils/create-form size-form)
             [size-form]
             ]
            (.getElementById js/document "app")))

(defn init! []
  (mount-root))
