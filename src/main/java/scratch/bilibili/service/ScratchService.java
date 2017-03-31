package scratch.bilibili.service;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import scratch.bilibili.dao.VideoScratchRecordDao;
import scratch.bilibili.dao.VideoTypeDao;
import scratch.bilibili.model.Video;
import scratch.bilibili.model.VideoScratchRecord;
import scratch.bilibili.model.VideoType;

@Service("scratchBilibiliService")
public class ScratchService  {

	private ExecutorService exec;
	
	private static String VIDEO_API = "http://api.bilibili.com/archive_rank/getarchiverankbypartion?"
			+ "callback=callback&type=jsonp"; //&tid=82&pn=7000
	
	private static int VIDEO_PAGE_SIZE = 1;
	
	@Autowired
	private VideoTypeDao typeDao;
	
	@Autowired
	private VideoScratchRecordDao recordDao;
	
	@Autowired
	private SaveRunnable saveRunnable;
	
	@Autowired
	private VideoPageReader pageReader;
	
	private boolean start = false;
	
//	@Scheduled(cron="50/60 * *  * * ? ")
	public void scratchVideo() {
		if(isRun()) return;
		exec = Executors.newFixedThreadPool(1000);
		start = true;
		//��ȡ��Ƶ����
		List<VideoType> types = typeDao.list(2);
		if(types == null || types.size() == 0) {
			return;
		}
		try{
			//����ÿ�������Ҫץȡ������
			Map<VideoType, BlockingQueue<String>> typeMap = new HashMap<>();
			for(VideoType t:types) {
				//�ȶ�ȡ��һҳ��URL����ȡ�������Ƶ��ҳ�����ܸ���
				List<Long> list = pageReader.read(VIDEO_API + "&tid=" + t.getCode() + "&pn=1");
				if(list.size() == 0) continue;
				long count = list.get(0);
				long pages = list.get(1);
				//����Type
				t.setVideoCount(count);
				typeDao.update(t);
				//��������ץȡ��ҳ��
				pages = pages > VIDEO_PAGE_SIZE ? VIDEO_PAGE_SIZE : pages;
				BlockingQueue<String> urls = new LinkedBlockingQueue<String>();
				for(int p=1; p<=pages; p++) {
					urls.add(VIDEO_API + "&tid=" + t.getCode() + "&pn=" + p);
				}
				typeMap.put(t, urls);
			}
			//׼��Record����
			VideoScratchRecord record = new VideoScratchRecord();
			record.setStartTime(new Date());
			//---------------------------�����̣߳�ִ����������------------------------------------
			//������Ƶ����������������ͬʱ���е��߳���
			//barrier�������1�������������ݱ����̵߳ģ��ȴ�����ץȡ��Ϻ��ٽ������ݱ���
			BlockingQueue<Video> videos =  new LinkedBlockingQueue<Video>();
			CyclicBarrier barrier = new CyclicBarrier(typeMap.size() + 1);
			for(BlockingQueue<String> urls:typeMap.values()) {
				exec.execute(new ScratchVideo(urls, videos, barrier));
			}
			exec.execute(saveRunnable.setVideos(videos, record, barrier));
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			exec.shutdown();
			start = false;
		}

	}
	
	/**
	 * ��ʱ���������߳��п����ģ����ǲ�����÷���ͬʱ���������ʱ���񣬱�������ͬ����
	 * @return
	 */
	public synchronized boolean isRun() {
		if(exec == null) return false;
		if(exec.isShutdown()) {
			if(exec.isTerminated()) {
				return false;
			} else {
				return true;
			}
		} else {
			return start;
		}
	}
	
	public VideoScratchRecord getRecentRecord() {
		return recordDao.getRecent();
	}

	public static void main(String[] args) throws MalformedURLException {
		ScratchService s = new ScratchService();
		s.scratchVideo();
	}

}

class ScratchVideo extends ScratchRunnable{

	private BlockingQueue<String> urls;
	
	private BlockingQueue<Video> videos;
	
	private VideoReader reader;
	
	private CyclicBarrier barrier;
	
	public ScratchVideo(BlockingQueue<String> urls, BlockingQueue<Video> videos,
			CyclicBarrier barrier) {
		this.urls = urls;
		this.videos = videos;
		this.barrier = barrier;
		this.reader = new VideoReader();
	}

	@Override
	public void scratch() {
		try{
			while(urls.size() > 0) {
				String url = urls.take();
				List<Video> list = reader.read(url);
				videos.addAll(list);
				log.debug(this + " ��ʣ������:" + urls.size());
			}
			log.debug(this + " ��ץȡ:" + videos.size());
		} catch (Exception e) {
			log.error(e.toString());
		} finally {
			try {
				//������finally��- -
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				log.error("Barrier��Դ�޷��ͷţ������߳̽������");
			}
		}
	}

}

class ScratchVideoSpecifi extends ScratchRunnable {

	private BlockingQueue<Video> sources;
	
	private BlockingQueue<Video> videos;
	
	private CyclicBarrier barrier;
	
	private VideoSpecificReader reader;
	
	private static final String VIDEO_URL = "http://api.bilibili.com/view?";
	
	private static final String APPKEY = "8e9fc618fbd41e28";
	
	public ScratchVideoSpecifi(BlockingQueue<Video> sources, 
			BlockingQueue<Video> videos, CyclicBarrier barrier) {
		this.sources = sources;
		this.videos = videos;
		this.barrier = barrier;
		this.reader = new VideoSpecificReader();
	}
	
	@Override
	public void scratch() {
		try {
			barrier.await();
			int size = sources.size();
			while(sources.size() > 0) {
				Video source = sources.take();
				if(source.getAvid() == null) {
					continue;
				}
				TimeUnit.MILLISECONDS.sleep(1500);
				String url = VIDEO_URL + "id=" + source.getAvid() + "&appkey=" + APPKEY;
				List<Video> vs = reader.read(url);
				if(vs == null) {
					log.warn(this + " �����쳣��ץȡ����ʧ��");
					Thread.interrupted();
				}
				videos.addAll(vs);	
				log.debug(this + " ��ʣ������:" + sources.size());
			}
			if(size != videos.size()) {
				log.warn(this + "ץȡvideo����Ŀ������:" + size + "��ʵ������:" + videos.size());
			} else {
				log.debug(this + " ��ץȡ:" + videos.size());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
		}
	}
	
}
